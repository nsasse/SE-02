/*-
 * Copyright (c) 1998 Brian Haskin jr.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */
package corewars.jmars;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.URL;
import java.io.*;

import corewars.jmars.marsVM.*;
import corewars.jmars.frontend.*;
import corewars.jmars.assembler.*;

/**
 * jMARS is a corewars interpreter in which programs (warriors) battle in the
 * memory of a virtual machine (the MARS) and try to disable the other program.
 */
public class jMARS extends Panel implements WindowListener, FrontEndManager {

    // constants
    static final int numDefinedColors = 4;
    static final Color wColors[][] = {{Color.green, Color.yellow},
            {Color.red, Color.magenta},
            {Color.cyan, Color.blue},
            {Color.gray, Color.darkGray}};

    // Application specific variables
    String args[];
    static Frame myFrame;
    static jMARS myApp;

    WarriorObj allWarriors[];
    WarriorObj warriors[];
    CoreDisplay coreDisplay;
    RoundCycleCounter roundCycleCounter;
    VM MARS;

    int runWarriors;

    static Thread myThread;
    static boolean exitFlag;

    Vector stepListeners;
    Vector cycleListeners;
    Vector roundListeners;

    public jMARS() {
        stepListeners = new Vector();
        cycleListeners = new Vector();
        roundListeners = new Vector();
    }

    /**
     * Starting function for the application. It sets up a frame and adds the
     * applet to it.
     *
     * @param java.lang.String[] a - array of command line arguments
     */
    public static void main(String args[]) {

        Result<Config> result = Config.CreateConfigFromArgs(args);

        if(!result.isOk()){
            System.out.println(result.getMessage());
            return;
        }

        myFrame = new Frame("jMARS");
        myFrame.setSize(new Dimension(1200, 900));
        myApp = new jMARS();
        myApp.args = args;
        myFrame.add(myApp);
        myFrame.addWindowListener(myApp);
        myFrame.show();
        myApp.applicationInit(result.getValue());
    }

    /**
     * Initialization function for the application.
     */
    void applicationInit(Config config) {

        Assembler parser = new corewars.jmars.assembler.icws94p.ICWS94p();
        parser.addConstant("coresize", Integer.toString(config.getCoreSize()));
        parser.addConstant("maxprocesses", Integer.toString(config.getMaxProc()));
        parser.addConstant("maxcycles", Integer.toString(config.getCycles()));
        parser.addConstant("maxlength", Integer.toString(config.getMaxWarriorLength()));
        parser.addConstant("mindistance", Integer.toString(config.getMinWarriorDistance()));
        parser.addConstant("rounds", Integer.toString(config.getRounds()));
        parser.addConstant("pspacesize", Integer.toString(config.getpSpaceSize()));
        parser.addConstant("warriors", Integer.toString(config.getNumWarriors()));
        allWarriors = new WarriorObj[config.getNumWarriors()];

        for (int i = 0; i < config.getNumWarriors(); i++) {
            try {
                FileInputStream wFile = new FileInputStream(args[(((Integer) config.getwArgs().elementAt(i)).intValue())]);
                try {
                    parser.parseWarrior(wFile);
                    if (parser.length() > config.getMaxWarriorLength()) {
                        System.out.println("Error: warrior " + args[(((Integer) config.getwArgs().elementAt(i)).intValue())] + " to large");
                        System.exit(0);
                    }
                    allWarriors[i] = new WarriorObj(parser.getWarrior(), parser.getStart(), wColors[i % numDefinedColors][0], wColors[i % numDefinedColors][1]);
                    allWarriors[i].setName(parser.getName());
                    allWarriors[i].setAuthor(parser.getAuthor());
                    allWarriors[i].Alive = true;
                    allWarriors[i].initPSpace(config.getpSpaceSize());
                    allWarriors[i].setPCell(0, -1);
                } catch (AssemblerException ae) {
                    System.out.println("Error parsing warrior file " + args[(((Integer) config.getwArgs().elementAt(i)).intValue())]);
                    System.out.println(ae.toString());
                    System.exit(0);
                } catch (IOException ioe) {
                    System.out.println("IO error while parsing warrior file " + args[(((Integer) config.getwArgs().elementAt(i)).intValue())]);
                    System.exit(0);
                }
            } catch (FileNotFoundException e) {
                System.out.println("Could not find warrior file " + args[(((Integer) config.getwArgs().elementAt(i)).intValue())]);
                System.exit(0);
            }
        }
        if (config.useGui())
        {
            coreDisplay = new CoreDisplay(this, this, config.getCoreSize(), 100);
        }
        roundCycleCounter = new RoundCycleCounter(this, this);
        validate();
        repaint();
        update(getGraphics());
        MARS = new MarsVM(config.getCoreSize(), config.getMaxProc());
        loadWarriors(config);
        config.setMinWarriors((config.getNumWarriors() == 1) ? 0 : 1);
        myThread = new Thread(() -> run(config));
        myThread.setPriority(Thread.NORM_PRIORITY - 1);
        myThread.start();
    }

    /**
     * main function and loop for jMARS. Runs the battles and handles display.
     */
    public void run(Config config) {
        HashMap<String, Integer> statistic = new HashMap<>();
        Date startTime;
        Date endTime;
        double roundTime;
        Date tStartTime;
        Date tEndTime;
        double totalTime;
        int totalCycles = 0;
        tStartTime = new Date();
        startTime = new Date();
        if (config.useGui())
        {
            coreDisplay.clear();
        }
        for (int roundNum = 0; roundNum < config.getRounds(); roundNum++) {
            int cycleNum = 0;
            for (; cycleNum < config.getCycles(); cycleNum++) {
                for (int warRun = 0; warRun < runWarriors; warRun++) {
                    StepReport stats = MARS.step();
                    stats.warrior.numProc = stats.numProc;
                    if (stats.wDeath) {
                        stats.warrior.Alive = false;
                        runWarriors--;
                        ArrayList<WarriorObj> tmp = new ArrayList<>();
                        for (int warIdx = 0; warIdx < warriors.length; warIdx++)
                        {
                            if (warIdx != warRun)
                            {
                                tmp.add(warriors[warIdx]);
                            }
                        }
                        warriors = tmp.toArray(new WarriorObj[] { });
                        break;
                    }
                    notifyStepListeners(stats);
                }
                notifyCycleListeners(cycleNum);
                repaint();
                if (runWarriors <= config.getMinWarriors()) {
                    break;
                }
            }
            for (int warIdx = 0; warIdx < warriors.length; warIdx++)
            {
                String name = warriors[warIdx].getName();
                Integer count = statistic.getOrDefault(name, Integer.valueOf(0));
                count++;
                statistic.put(name, count);
            }
            notifyRoundListeners(roundNum);
            endTime = new Date();
            roundTime = ((double) endTime.getTime() - (double) startTime.getTime()) / 1000;
            System.out.println(roundNum+1 + ". Round time=" + roundTime + " Cycles=" + cycleNum + " avg. time/cycle=" + (roundTime / cycleNum));
            startTime = new Date();
            totalCycles += cycleNum;
            if (exitFlag) {
                break;
            }
            MARS.reset();
            loadWarriors(config);
            if (config.useGui())
            {
                coreDisplay.clear();
            }
        }
        tEndTime = new Date();
        totalTime = ((double) tEndTime.getTime() - (double) tStartTime.getTime()) / 1000;
        System.out.println("Total time=" + totalTime + " Total Cycles=" + totalCycles + " avg. time/cycle=" + (totalTime / totalCycles));
        System.out.println("Survivor in how many rounds:");
        for (String name : statistic.keySet())
        {
            System.out.println("  " + name + ": " + statistic.get(name));
        }
    }

    /**
     * Load warriors into core
     */
    void loadWarriors(Config config) {
        warriors = new WarriorObj[allWarriors.length];
        System.arraycopy(allWarriors, 0, warriors, 0, allWarriors.length);
        runWarriors = config.getNumWarriors();
        int[] location = new int[warriors.length];

        if (!MARS.loadWarrior(warriors[0], 0)) {
            System.out.println("ERROR: could not load warrior 1.");
        }

        for (int i = 1, r = 0; i < config.getNumWarriors(); i++) {
            boolean validSpot;
            do {
                validSpot = true;
                r = (int) (Math.random() * config.getCoreSize());

                if (r < config.getMinWarriorDistance() || r > (config.getCoreSize() - config.getMinWarriorDistance())) {
                    validSpot = false;
                }

                for (int j = 0; j < location.length; j++) {
                    if (r < (config.getMinWarriorDistance() + location[j]) && r > (config.getMinWarriorDistance() + location[j])) {
                        validSpot = false;
                    }
                }
            } while (!validSpot);

            if (!MARS.loadWarrior(warriors[i], r)) {
                System.out.println("ERROR: could not load warrior " + (i + 1) + ".");
            }
        }
    }

    /**
     * update the display
     *
     * @param java.awt.Graphics g - Graphics context
     */
    public void update(Graphics g) {
        paintComponents(g);
        return;
    }

    /**
     * register an object to receive step results.
     *
     * @param StepListener - object to register
     */
    public void registerStepListener(StepListener l) {
        stepListeners.addElement(l);
    }

    /**
     * register an object to receive cycle results.
     *
     * @param CycleListener - object to register
     */
    public void registerCycleListener(CycleListener c) {
        cycleListeners.addElement(c);
    }

    /**
     * register an object to receive round results.
     *
     * @param RoundListener - object to register
     */
    public void registerRoundListener(RoundListener r) {
        roundListeners.addElement(r);
    }

    protected void notifyStepListeners(StepReport step) {
        for (Enumeration e = stepListeners.elements(); e.hasMoreElements();) {
            StepListener j = (StepListener) e.nextElement();
            j.stepProcess(step);
        }
    }

    protected void notifyCycleListeners(int cycle) {
        for (Enumeration e = cycleListeners.elements(); e.hasMoreElements();) {
            CycleListener j = (CycleListener) e.nextElement();
            j.cycleFinished(cycle);
        }
    }

    protected void notifyRoundListeners(int round) {
        for (Enumeration e = roundListeners.elements(); e.hasMoreElements();) {
            RoundListener j = (RoundListener) e.nextElement();
            j.roundResults(round);
        }
    }

    /**
     * Invoked when a window is in the process of being closed. The close
     * operation can be overridden at this point.
     */
    public void windowClosing(WindowEvent e) {
        exitFlag = true;
        System.exit(0);
    }

    /**
     * Invoked when a window has been opened.
     */
    public void windowOpened(WindowEvent e) {

    }

    /**
     * Invoked when a window has been closed.
     */
    public void windowClosed(WindowEvent e) {

    }

    /**
     * Invoked when a window is iconified.
     */
    public void windowIconified(WindowEvent e) {

    }

    /**
     * Invoked when a window is de-iconified.
     */
    public void windowDeiconified(WindowEvent e) {

    }

    /**
     * Invoked when a window is activated.
     */
    public void windowActivated(WindowEvent e) {

    }

    /**
     * Invoked when a window is de-activated.
     */
    public void windowDeactivated(WindowEvent e) {

    }
}

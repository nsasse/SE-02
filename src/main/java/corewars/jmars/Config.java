package corewars.jmars;

import java.util.Vector;

public class Config {
    private boolean useGui;
    private int maxProc;
    private int pSpaceSize;
    private int coreSize;
    private int cycles;
    private int rounds;
    private int maxWarriorLength;
    private int minWarriorDistance;
    private int numWarriors;
    private int minWarriors;
    private Vector wArgs;

    public Config(){
        this.useGui = false;
        this.maxWarriorLength = 100;
        this.minWarriorDistance = 100;
        this.maxProc = 8000;
        this.coreSize = 8000;
        this.cycles = 80000;
        this.rounds = 10;
        this.wArgs = new Vector();
    }

    public static Result<Config> CreateConfigFromArgs(String[]args){

        if (args.length == 0) {
            return new Result<>(false, "usage: jMARS [options] warrior1.red [warrior2.red ...]");
        }

        Config config = new Config();

        boolean pspaceChanged = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].charAt(0) == '-') {
                switch(args[i]){
                    case "-g":
                        config.useGui = true;
                        break;
                    case "-s":
                        config.coreSize = Integer.parseInt(args[++i]);
                        break;
                    case "-r":
                        config.rounds = Integer.parseInt(args[++i]);
                        break;
                    case "-c":
                        config.cycles = Integer.parseInt(args[++i]);
                        break;
                    case "-p":
                        config.maxProc = Integer.parseInt(args[++i]);
                        break;
                    case "-l":
                        config.maxWarriorLength = Integer.parseInt(args[++i]);
                        break;
                    case "-d":
                        config.minWarriorDistance = Integer.parseInt(args[++i]);
                        break;
                    case "S":
                        config.pSpaceSize = Integer.parseInt(args[++i]);
                        pspaceChanged = true;
                        break;
                    default:
                        return new Result<>(false, "Invalid program argument: " + args[i]);
                }
            } else {
                config.numWarriors++;
                config.wArgs.addElement(new Integer(i));
            }
        }

        if (!pspaceChanged) {
            config.pSpaceSize = config.coreSize / 16;
        }

        if (config.numWarriors == 0) {
            return new Result<>(false, "ERROR: no warrior files specified");
        }

        return new Result<>(true, config);
    }

    public boolean useGui() {
        return useGui;
    }

    public int getMaxProc() {
        return maxProc;
    }

    public int getpSpaceSize() {
        return pSpaceSize;
    }

    public int getCoreSize() {
        return coreSize;
    }

    public int getCycles() {
        return cycles;
    }

    public int getRounds() {
        return rounds;
    }

    public int getMaxWarriorLength() {
        return maxWarriorLength;
    }

    public int getMinWarriorDistance() {
        return minWarriorDistance;
    }

    public int getNumWarriors() {
        return numWarriors;
    }

    public int getMinWarriors() {
        return minWarriors;
    }

    public Vector getwArgs() {
        return wArgs;
    }

    public void setMinWarriors(int minWarriors) {
        this.minWarriors = minWarriors;
    }
}

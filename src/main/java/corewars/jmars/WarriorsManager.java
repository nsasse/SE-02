package corewars.jmars;

import corewars.jmars.assembler.Assembler;
import corewars.jmars.assembler.AssemblerException;
import corewars.jmars.marsVM.VM;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class WarriorsManager {

    public WarriorsManager() {
    }

    Result<WarriorObj[]> loadWarriors(Config config, VM MARS, WarriorObj[] allWarriors) {

        WarriorObj[] warriors = new WarriorObj[allWarriors.length];
        System.arraycopy(allWarriors, 0, warriors, 0, allWarriors.length);

        int[] location = new int[warriors.length];

        if (!MARS.loadWarrior(warriors[0], 0)) {
            return new Result<>(false, "ERROR: could not load warrior 1.");
        }

        for (int i = 1, r; i < config.getNumWarriors(); i++) {
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
                return new Result<>(false, "ERROR: could not load warrior " + (i + 1) + ".");
            }
        }
        return new Result<>(true, warriors);
    }

    Result<WarriorObj[]> readWarriorsFromFile(Config config, Assembler parser, Color[][] wColors, int numDefinedColors, String[] args) {

        WarriorObj[] allWarriors = new WarriorObj[config.getNumWarriors()];

        for (int i = 0; i < config.getNumWarriors(); i++) {
            try {
                FileInputStream wFile = new FileInputStream(args[(((Integer) config.getwArgs().elementAt(i)).intValue())]);
                try {
                    parser.parseWarrior(wFile);
                    if (parser.length() > config.getMaxWarriorLength()) {
                        return new Result<>(false, "Error: warrior " + args[(((Integer) config.getwArgs().elementAt(i)).intValue())] + " to large");
                    }
                    allWarriors[i] = new WarriorObj(parser.getWarrior(), parser.getStart(), wColors[i % numDefinedColors][0], wColors[i % numDefinedColors][1], parser.getName(), parser.getAuthor(), true, config.getpSpaceSize(), 0, -1);
                } catch (AssemblerException ae) {
                    return new Result<>(false, "Error parsing warrior file " + args[(((Integer) config.getwArgs().elementAt(i)).intValue())] + "\n" + ae.toString());
                } catch (IOException ioe) {
                    return new Result<>(false, "IO error while parsing warrior file " + args[(((Integer) config.getwArgs().elementAt(i)).intValue())]);
                }
            } catch (FileNotFoundException e) {
                return new Result<>(false, "Could not find warrior file " + args[(((Integer) config.getwArgs().elementAt(i)).intValue())]);
            }
        }

        return new Result<>(true, allWarriors);
    }
}

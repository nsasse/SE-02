package corewars.jmars;

import corewars.jmars.marsVM.VM;

public class WarriorsLogic {

    WarriorObj[] warriors;
    int runWarriors;

    public WarriorsLogic() {
    }

    public WarriorsLogic(int runWarriors, WarriorObj[] warriors){
        this.warriors = warriors;
        this.runWarriors = runWarriors;
    };

    static WarriorsLogic load(Config config, int runWarriors, VM MARS, WarriorObj allWarriors[], WarriorObj[] warriors) {
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
        return new WarriorsLogic(runWarriors, warriors);
    }
}

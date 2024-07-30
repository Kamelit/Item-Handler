package org.minecrafttest.main.Parkour.Scores;


public class PlayerInfo {
    private final double completionPercentage;
    private final double overallCompletionPercentage;
    private final int position;

    public PlayerInfo(double completionPercentage, double overallCompletionPercentage, int position) {
        this.completionPercentage = completionPercentage;
        this.overallCompletionPercentage = overallCompletionPercentage;
        this.position = position;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public double getOverallCompletionPercentage() {
        return overallCompletionPercentage;
    }

    public int getPosition() {
        return position;
    }
}
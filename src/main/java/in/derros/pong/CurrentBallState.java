package in.derros.pong;

/**
 * Created by derros on 5/17/17.
 */
private class CurrentBallState {
    double velX;
    double velY;
    double accX;
    double accY;
    BallStates state;
    private static CurrentBallState ballStateInstance = null;
    private CurrentBallState(double velX, double velY, double accX, double accY, BallStates state) {
        this.velX = velX;
        this.velY = velY;
        this.accX = accX;
        this.accY = accY;
        this.state = state;
    }

    static CurrentBallState getCurrentBallState(double velX, double velY, double accX, double accY, BallStates state) {
        if(ballStateInstance == null) {
            ballStateInstance = new CurrentBallState(velX, velY, accX, accY, state);
            return ballStateInstance;
        } else {
            ballStateInstance.calculateNextMove(prevX, prevY, currX, currY, velX, velY);
        }
    }

    double getVelX() {
        return velX;
    }

    void setVelX(double vel) {
        this.velX = vel;
    }

    double getVelY() {
        return velY;
    }

    void setVelY(double vel) {
        this.velY = vel;
    }

    double getAccX() {
        return accX;
    }

    void setAccX(double acc) {
        this.accX = acc;
    }

    double getAccY() {
        return accY;
    }

    void setAccY(double acc) {
        this.accY = acc;
    }

    BallStates getState() {
        return state;
    }

    void changeBallState(BallStates state) {
        this.state = state;
    }

    double calculateNextMove(double prevX, double prevY, double currX, double currY, double prevVelX, double prevVelY) {
        // get curr velocity
        double currVelX =
    }

}
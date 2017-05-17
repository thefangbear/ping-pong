package in.derros.pong;


import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Created by derros on 5/16/17.
 */
public class Table extends Application {

    private static double speed = 1;

    private final int BOUNDARY_X_LEFT = 10;
    private final int BOUNDARY_X_RIGHT = 790;
    private final int BOUNDARY_Y_TOP = 10;
    private final int BOUNDARY_Y_BOTTOM = 760;
    private final int CENTER_X = 500;
    private final int CENTER_Y = 400;
    private volatile boolean isReady = false;
    private volatile Double selfPadAccelerationX = .0;
    private volatile Double competitorPadAccelerationX = .0;

    public static void startShow() {
        launch();
    }

    public static void startShow(int speed) {
        Table.speed = speed;
        launch();
    }

    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("DistPP");
        Rectangle competitorRect = new Rectangle(500, BOUNDARY_Y_TOP, 200, 10);
        Rectangle selfRect = new Rectangle(500, BOUNDARY_Y_BOTTOM, 200, 10);
        selfRect.setOnMouseDragged((e) -> {
            selfRect.setX((e.getSceneX() > BOUNDARY_X_RIGHT) ?
                    BOUNDARY_X_RIGHT : (e.getSceneX() < BOUNDARY_X_LEFT ? BOUNDARY_X_LEFT : e.getSceneX()));
        });
        Circle ball = new Circle(500, 500, 30);
        Group root = new Group();
        root.getChildren().add(competitorRect);
        root.getChildren().add(selfRect);
        root.getChildren().add(ball);
        primaryStage.setScene(new Scene(root, 1000, 800));
        (new Thread(() -> {
            double selfPadX = selfRect.getX();
            double competitorPadX = competitorRect.getX();
            double velS = .0, velC = .0;
            double accS = .0, accC = .0;
            while(!isReady);
            while(primaryStage.isShowing()) {
                // calculate acceleration of pad
                // pad acc = net change in net change
                    accS = (selfRect.getX()-selfPadX) - velS;
                    accC = (competitorRect.getX()-competitorPadX) - velC;
                    velS = selfRect.getX() - selfPadX;
                    velC = competitorRect.getX() - competitorPadX;
                    selfPadX = selfRect.getX();
                    competitorPadX = competitorRect.getX();
                    synchronized(selfPadAccelerationX) {
                        selfPadAccelerationX = accS;
                    }
                    synchronized(competitorPadAccelerationX) {
                        competitorPadAccelerationX = accC;
                    }
            }
        })).start();
        (new Thread(() -> {
            double initialY = CENTER_Y,
                   initialX = CENTER_X,
                    currY = ball.getCenterY(),
                    currX = ball.getCenterX(),
                    accX = .0, accY = .1,
                    velX = 2, velY = 2;
            BallStates ballstate = BallStates.InitialState;
            while (!isReady) ;
            while (primaryStage.isShowing()) {
                while (isReady) {
                    try {
                        Thread.sleep(Math.round(100 * speed));
                    } catch (InterruptedException ignored) {}
                    synchronized (ball) {
                        switch (ballstate) {
                            case NormalState:
                            case InitialState: {
                                // apply velocity
                                currX += velX;
                                currY += velY;
                                // apply acceleration
                                velX += accX;
                                velY += accY;
                                // apply
                                ball.setCenterX(currX);
                                ball.setCenterY(currY);
                                // refresh
                                currX = ball.getCenterX();
                                currY = ball.getCenterY();
                                // set & detect
                                ballstate = BallStates.TransitionalState;
                                break;
                            }
                            case BumpedLeftWallState:
                            case BumpedRightWallState: {
                                // reverse vel/acceleration, wall is bumpy
                                velX = -velX;
                                accX = -accX + .01;
                                currX += velX;
                                currY += velY;
                                velX += accX;
                                velY += accY;
                                ball.setCenterX(currX);
                                ball.setCenterY(currY);
                                // set & detect
                                ballstate = BallStates.TransitionalState;
                                break;
                            }
                            case BumpedTopWallState:
                            case BumpedBottomWallState: {
                                // reverse vel/acc in y-direction, wall is bumpy
                                velY = -velY;
                                accY = -accY;
                                currX += velX;
                                currY += velY;
                                velX += accX;
                                velY += accY;
                                ball.setCenterX(currX);
                                ball.setCenterY(currY);
                                ballstate = BallStates.NormalState;
                                break;
                            }
                            case BumpedSelfPad: {
                                synchronized(selfPadAccelerationX) {
                                    double padAccX = selfPadAccelerationX;
                                    accY = -accY;
                                    velY = -velY; // veRY bumPY
                                    accX += padAccX;
                                    ballstate = BallStates.NormalState;
                                }
                            }
                            case BumpedCompetitorPad: {
                                synchronized (competitorPadAccelerationX) {
                                    double padAccX = competitorPadAccelerationX;
                                }
                            }
                            case TransitionalState: {

                            }
                        }
                    }
                }
            }
        })).start();
        primaryStage.setOnCloseRequest((e) -> {
            isReady = false;
        });
        primaryStage.show();

        isReady = true;
    }

    private enum BallStates {
        InitialState,
        BumpedRightWallState,
        BumpedLeftWallState,
        BumpedTopWallState,
        BumpedBottomWallState,
        TransitionalState,
        NormalState,
        BumpedSelfPad,
        BumpedCompetitorPad
    }


}

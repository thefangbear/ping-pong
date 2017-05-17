package in.derros.pong;


import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by derros on 5/16/17.
 */
public class Table extends Application {

    private static double speed = 1;

    private final int BOUNDARY_X_LEFT = 10;
    private final int BOUNDARY_X_RIGHT = 790;
    private final int BOUNDARY_Y_TOP = 10;
    private final int BOUNDARY_Y_BOTTOM = 760;
    private final int BALL_RADIUS = 30;
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
        Circle ball = new Circle(CENTER_X, CENTER_Y, BALL_RADIUS);
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
            while (!isReady) ;
            while (primaryStage.isShowing()) {
                try {
                    Thread.sleep(10); // we need a little while
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // calculate acceleration of pad
                // pad acc = net change in net change
                accS = (selfRect.getX() - selfPadX) - velS;
                accC = (competitorRect.getX() - competitorPadX) - velC;
                velS = selfRect.getX() - selfPadX;
                velC = competitorRect.getX() - competitorPadX;
                selfPadX = selfRect.getX();
                competitorPadX = competitorRect.getX();
                synchronized (selfPadAccelerationX) {
                    selfPadAccelerationX = accS;
                }
                synchronized (competitorPadAccelerationX) {
                    competitorPadAccelerationX = accC;
                }
            }
        })).start();
        (new Thread(() -> {
            while(!isReady);
            while(primaryStage.isShowing()) {
                switch(Main.operating_mode) {
                    case 1: // server
                        synchronized(Volatiles.newestPongCompetitorLocation) {
                            competitorRect.setX(Volatiles.newestPongCompetitorLocation);
                            try {
                                RealPong.createPongServer().sendSelfXCoord(selfRect.getX());
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                        break;
                    case 0: // client
                        synchronized(Volatiles.newestPingCompetitorLocation) {
                            competitorRect.setX(Volatiles.newestPingCompetitorLocation);
                            try {
                                RealPing.getRealPing().sendSelfXCoord(selfRect.getX());
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                        break;
                }
            }
        })).start();
        (new Thread(() -> {
            double  initialY = CENTER_Y,
                    initialX = CENTER_X,
                    currY = ball.getCenterY(),
                    currX = ball.getCenterX(),
                    accX = .0, accY = .1,
                    velX = 0, velY = 2;
            BallStates ballstate = BallStates.InitialState;
            BallStates ballstate2 = BallStates.NullState;
            while (!isReady) ;
            while (primaryStage.isShowing()) {
                while (isReady) {
                    try {
                        Thread.sleep(Math.round(100 * speed));
                    } catch (InterruptedException ignored) {
                    }
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
                                ballstate = BallStates.NormalState;
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
                                synchronized (selfPadAccelerationX) {
                                    double padAccX = selfPadAccelerationX;
                                    accY = -accY;
                                    velY = -velY; // veRY bumPY
                                    accX += padAccX;
                                    ballstate = BallStates.NormalState;
                                }
                                break;
                            }
                            case BumpedCompetitorPad: {
                                synchronized (competitorPadAccelerationX) {
                                    double padAccX = competitorPadAccelerationX;
                                    accY = -accY;
                                    velY = -velY;
                                    accX += padAccX;
                                    ballstate = BallStates.NormalState;
                                }
                                break;
                            }
                            case TransitionalState: {
                                // check if touched boundaries
                                if (ballstate2 != BallStates.NullState) {
                                    System.out.println("Ball State 2 triggered when ball is " + ballstate + "bs2 is " + ballstate2);
                                    ballstate = ballstate2;
                                    ballstate2 = BallStates.NullState;
                                    continue; // go on for another iteration to handle concurrent edge cases
                                }
                                System.out.println("begin transition");
                                if (currX >= BOUNDARY_X_RIGHT) {
                                    System.out.println("touched right boundary");
                                    ballstate = BallStates.BumpedRightWallState;
                                    if (currY <= BOUNDARY_Y_TOP) {
                                        ballstate2 = BallStates.BumpedTopWallState;
                                    } else if (currY >= BOUNDARY_Y_BOTTOM) {
                                        ballstate2 = BallStates.BumpedBottomWallState;
                                    } else if (currX >= selfRect.getX() - 100 && currX <= selfRect.getX() + 100
                                            && currY + BALL_RADIUS <= selfRect.getY() + 5) {
                                        ballstate2 = BallStates.BumpedSelfPad;
                                    } else if (currX >= competitorRect.getX() - 100 && currX <= competitorRect.getX() + 100
                                            && currY - BALL_RADIUS <= competitorRect.getY() - 5) {
                                        ballstate2 = BallStates.BumpedCompetitorPad;
                                    }

                                } else if (currX <= BOUNDARY_X_LEFT) {
                                    System.out.println("touched left boundary");
                                    ballstate = BallStates.BumpedLeftWallState;
                                    if (currY <= BOUNDARY_Y_TOP) {
                                        ballstate2 = BallStates.BumpedTopWallState;
                                    } else if (currY >= BOUNDARY_Y_BOTTOM) {
                                        ballstate2 = BallStates.BumpedBottomWallState;
                                    } else if (currX >= selfRect.getX() - 100 && currX <= selfRect.getX() + 100
                                            && currY + BALL_RADIUS <= selfRect.getY() + 5) {
                                        ballstate2 = BallStates.BumpedSelfPad;
                                    } else if (currX >= competitorRect.getX() - 100 && currX <= competitorRect.getX() + 100
                                            && currY - BALL_RADIUS <= competitorRect.getY() - 5) {
                                        ballstate2 = BallStates.BumpedCompetitorPad;
                                    }
                                } else if (currY >= BOUNDARY_Y_BOTTOM) {
                                    System.out.println("touched bottom");
                                    ballstate = BallStates.BumpedBottomWallState;
                                    if (currX >= BOUNDARY_X_RIGHT) {
                                        ballstate2 = BallStates.BumpedRightWallState;
                                    } else if (currX <= BOUNDARY_X_LEFT) {
                                        ballstate2 = BallStates.BumpedLeftWallState;
                                    } else if (currX >= selfRect.getX() - 100 && currX <= selfRect.getX() + 100
                                            && currY + BALL_RADIUS <= selfRect.getY() + 5) {
                                        ballstate2 = BallStates.BumpedSelfPad;
                                    } else if (currX >= competitorRect.getX() - 100 && currX <= competitorRect.getX() + 100
                                            && currY - BALL_RADIUS <= competitorRect.getY() - 5) {
                                        ballstate2 = BallStates.BumpedCompetitorPad;
                                    }
                                } else if (currY <= BOUNDARY_Y_TOP) {
                                    System.out.println("touched top");
                                    ballstate = BallStates.BumpedTopWallState;
                                    if (currX >= BOUNDARY_X_RIGHT) {
                                        ballstate2 = BallStates.BumpedRightWallState;
                                    } else if (currX <= BOUNDARY_X_LEFT) {
                                        ballstate2 = BallStates.BumpedLeftWallState;
                                    } else if (currX >= selfRect.getX() - 100 && currX <= selfRect.getX() + 100
                                            && currY + BALL_RADIUS <= selfRect.getY() + 5) {
                                        ballstate2 = BallStates.BumpedSelfPad;
                                    } else if (currX >= competitorRect.getX() - 100 && currX <= competitorRect.getX() + 100
                                            && currY - BALL_RADIUS <= competitorRect.getY() - 5) {
                                        ballstate2 = BallStates.BumpedCompetitorPad;
                                    }
                                } else if (currX >= selfRect.getX() - 100 && currX <= selfRect.getX() + 100
                                        && currY + BALL_RADIUS >= selfRect.getY() + 5) {
                                    System.out.println("bumped on self.");
                                    ballstate = BallStates.BumpedSelfPad;
                                    if (currX >= BOUNDARY_X_RIGHT) {
                                        ballstate2 = BallStates.BumpedRightWallState;
                                    } else if (currX <= BOUNDARY_X_LEFT) {
                                        ballstate2 = BallStates.BumpedLeftWallState;
                                    }
                                } else if (currX >= competitorRect.getX() - 100 && currX <= competitorRect.getX() + 100
                                        && currY - BALL_RADIUS <= competitorRect.getY() - 5) {
                                    System.out.println("bumped on competitor");
                                    ballstate = BallStates.BumpedCompetitorPad;
                                    if (currX >= BOUNDARY_X_RIGHT) {
                                        ballstate2 = BallStates.BumpedRightWallState;
                                    } else if (currX <= BOUNDARY_X_LEFT) {
                                        ballstate2 = BallStates.BumpedLeftWallState;
                                    }
                                } else {
                                    ballstate = BallStates.NormalState;
                                }
                                break;
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
        BumpedCompetitorPad,
        NullState
    }


}

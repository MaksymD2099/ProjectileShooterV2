/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projectileshooter;

import java.awt.Point;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author cstuser
 */
public class FXMLDocumentController implements Initializable {

    private double lastFrameTime = 0.0;
    
    @FXML
    private AnchorPane pane;
    
    @FXML
    private Label coordinates;

    @FXML
    private ImageView gunImage;

    @FXML
    private ImageView character;

    @FXML
    private RadioButton radioButtonFireGun;
    @FXML
    private RadioButton radioButtonIceGun;
    @FXML
    private RadioButton radioButtonAGGun;
    @FXML
    private RadioButton radioButtonPortalGun;
        
    //Local Variables
    ArrayList<Double> values = new ArrayList<Double>();
    public ArrayList<GameObject> objectList = new ArrayList<>();
    private ArrayList<Projectile> arrayListProjectiles = new ArrayList<>();

    private int counterProjectiles;
    private int counterBounces;

    //rectangles for detecting collisions with the edges of the game environment
    private Edge edgeRoof;
    private Edge edgeFloor;
    private Edge edgeLeftWall;
    private Edge edgeRightWall;

    //private Gun gun;
    private boolean increasing = true;

    //Boolean to check if the projectile is within the bounds of an antigravity region    
    private boolean isWithinGravity = false;
    //private boolean decreasing = false;

    public Point mouseAim;
    public Point gunPivot;

    //used in the calculation of the angle of the gun and the projectile initial velocity
    double theta;

    //Variables for the mouse's position within the game environment    
    private double mouseX;
    private double mouseY;

    private Vector velocityProjectile;
    private Vector acceleration;
    private Projectile projectile;

    //Final variables 
    private final double PROJECTILE_RADIUS = 10; // Radius of the projectiles
    private final double PROJECTILE_VELOCITY = 500; //Magnitude of the projectile's velocity
    private final double GRAVITY = 50; //Magnitude of gravity    
    private final int MAX_NUMBER_OF_BOUNCES = 1;
    private final double GUN_LENGTH = 243;

    //String to know what kind of gun is being used -----Not implemented anywhere yet but will be useful
    private String selectedGunType;

    public void addToPane(Node node) {
        pane.getChildren().add(node);
    }

    public void removeFromPane(Node node) {
        pane.getChildren().remove(node);
    }

    @FXML
    public void mouseMoved(MouseEvent event) {
        mouseX = event.getSceneX();
        mouseY = (pane.getHeight() - event.getSceneY());

        mouseAim = new Point((int) mouseX, (int) mouseY);
        gunRotationAngle(mouseAim, gunPivot);

        coordinates.setText("MouseX: " + mouseX + "MouseY: " + mouseY);
    }

    @FXML
    public void mouseClicked(MouseEvent event) {
        
        acceleration = new Vector(0, GRAVITY);

        //Checks if the projectile is within the bounds of an antigravity region and inverts the gravity if it is.
        if (isWithinGravity) {
            acceleration = new Vector(0, -GRAVITY);
        }

        //Supplies the method with values for the mouse's "x" and "y" coordinates
        mouseX = event.getSceneX();
        mouseY = (pane.getHeight() - event.getSceneY());

        //acceleration vector to be used in the calculation of the projectile's flight at the moment the mouse is clicked
        //---------Changes the type of projectile depending on the gun--------
        if (gunImage.getImage().toString() == AssetManager.getGunFire_Img().toString()) {
            projectile = new Projectile(new Vector(getGunTip().getX(), getGunTip().getY()), new Vector(Math.cos(theta) * PROJECTILE_VELOCITY, -Math.sin(theta) * PROJECTILE_VELOCITY), acceleration, PROJECTILE_RADIUS, "fire");         
        }

        if (gunImage.getImage().toString() == AssetManager.getGunIce_Img().toString()) {
            projectile = new Projectile(new Vector(gunImage.getLayoutX() + 100, gunImage.getLayoutY()), new Vector(Math.cos(theta) * PROJECTILE_VELOCITY, -Math.sin(theta) * PROJECTILE_VELOCITY), acceleration, PROJECTILE_RADIUS, "ice");
        }

        if (gunImage.getImage().toString() == AssetManager.getGunAntiGravity_Img().toString()) {
            projectile = new Projectile(new Vector(gunImage.getLayoutX() + 100, gunImage.getLayoutY()), new Vector(Math.cos(theta) * PROJECTILE_VELOCITY, -Math.sin(theta) * PROJECTILE_VELOCITY), acceleration, PROJECTILE_RADIUS, "antiGravity");
        }

        if (gunImage.getImage().toString() == AssetManager.getGunPortalIn_Img().toString()) {
            projectile = new Projectile(new Vector(gunImage.getLayoutX() + 100, gunImage.getLayoutY()), new Vector(Math.cos(theta) * PROJECTILE_VELOCITY, -Math.sin(theta) * PROJECTILE_VELOCITY), acceleration, PROJECTILE_RADIUS, "portalIn");
        }

        if (gunImage.getImage().toString() == AssetManager.getGunPortalOut_Img().toString()) {
            projectile = new Projectile(new Vector(gunImage.getLayoutX() + 100, gunImage.getLayoutY()), new Vector(Math.cos(theta) * PROJECTILE_VELOCITY, -Math.sin(theta) * PROJECTILE_VELOCITY), acceleration, PROJECTILE_RADIUS, "portalOut");
        }

        //This ensures that only one projectile can exist at one time and if it can exist, it adds it to the scene
        if (arrayListProjectiles.isEmpty()) {
            addToPane(projectile.getCircle());
            objectList.add(projectile);
            arrayListProjectiles.add(projectile);
        }
    }

    public void gunRotationAngle(Point mouseAim, Point gunPivot) {
        theta = Math.atan2(mouseAim.y - gunPivot.y, mouseAim.x - gunPivot.x);
        double angle = Math.toDegrees(theta);
        //System.out.println(angle + "");      
        gunImage.setRotate(-angle);
    }
    
    public Point getGunTip()
    {
        //projectile = new Projectile(new Vector(gunImage.getLayoutX() + 100, gunImage.getLayoutY()), new Vector(Math.cos(theta) * PROJECTILE_VELOCITY, -Math.sin(theta) * PROJECTILE_VELOCITY), acceleration, PROJECTILE_RADIUS, "fire");
        Point gunTip;
    
        //Vector gunTip = new Vector(gunImage.getLayoutX() + Math.cos(theta) * 25.0, gunImage.getLayoutY() - Math.sin(theta) * 25.0);
        
        gunTip = new Point((int)(gunImage.getLayoutX() + Math.cos(theta) * GUN_LENGTH), (int)(gunImage.getLayoutY() - Math.sin(theta) * GUN_LENGTH));
        
        return gunTip;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lastFrameTime = 0.0f;
        long initialTime = System.nanoTime();

        AssetManager.preloadAllAssets();

        
        ToggleGroup group = new ToggleGroup();      
        radioButtonFireGun.setToggleGroup(group);
        radioButtonIceGun.setToggleGroup(group);
        radioButtonAGGun.setToggleGroup(group);
        radioButtonPortalGun.setToggleGroup(group);
                
        
        //Image of MainCharacter
        character.setImage(AssetManager.getCharacterImage());

        //Image Gun
        //if(selectedGunType == "fire")
        gunImage.setImage(AssetManager.getGunFire_Img());      
        radioButtonFireGun.setSelected(true);
        
        gunPivot = new Point();
        gunPivot.setLocation(100, 100);

        //Creating Edge objects
        edgeFloor = new Edge(new Vector(0, pane.getPrefHeight() + 50), pane.getPrefWidth() + 50, 1);
        edgeRoof = new Edge(new Vector(0, 0), pane.getPrefWidth() + 50, 1);
        edgeLeftWall = new Edge(new Vector(0, 0), 1, pane.getPrefHeight() + 50);
        edgeRightWall = new Edge(new Vector(pane.getPrefWidth() + 50, 0), 1, pane.getPrefHeight() + 50);

        //Adding Edges to the pane so that collisions can be detected with the edge        
        addToPane(edgeFloor.getRectangle());
        addToPane(edgeRoof.getRectangle());
        addToPane(edgeLeftWall.getRectangle());
        addToPane(edgeRightWall.getRectangle());

        //Adding edges to the objectList so that their existance within the program can be monitored                
        objectList.add(edgeFloor);
        objectList.add(edgeRoof);
        objectList.add(edgeLeftWall);
        objectList.add(edgeRightWall);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    double currentTime = (now - initialTime) / 1000000000.0;
                    double frameDeltaTime = currentTime - lastFrameTime;
                    lastFrameTime = currentTime;

                    for (GameObject obj : objectList) {
                        if (obj != null) {
                            obj.updateRectangle(frameDeltaTime);
                            obj.updateCircle(frameDeltaTime);
                        }
                    }
                } catch (Exception e) {}
                
                if(group.getSelectedToggle() == radioButtonFireGun)
                {
                    gunImage.setImage(AssetManager.getGunFire_Img());        
                }
                if(group.getSelectedToggle() == radioButtonIceGun)
                {
                    gunImage.setImage(AssetManager.getGunIce_Img());        
                }
                if(group.getSelectedToggle() == radioButtonAGGun)
                {
                    gunImage.setImage(AssetManager.getGunAntiGravity_Img());        
                }
                if(group.getSelectedToggle() == radioButtonPortalGun)
                {
                    gunImage.setImage(AssetManager.getGunPortalIn_Img());        
                }
                
                for (int i = 0; i < arrayListProjectiles.size(); i++) {
                    //AudioClip tempBounce = AssetManager.getBounce();  //---------------------SOUND IS BROKEN---------------
                    Projectile tempProjectile = arrayListProjectiles.get(i);

                    Circle projectileCircle = tempProjectile.getCircle();
                    Bounds boundProjectileCircle = projectileCircle.getBoundsInParent();

                    //Setting bounds for the edges of the game environment               
                    //Bounds of floor
                    Rectangle rectangleEdgeFloor = edgeFloor.getRectangle();
                    Bounds boundRectangleEdgeFloor = rectangleEdgeFloor.getBoundsInParent();

                    Rectangle rectangleEdgeRoof = edgeRoof.getRectangle();
                    Bounds boundRectangleEdgeRoof = rectangleEdgeRoof.getBoundsInParent();

                    Rectangle rectangleEdgeLeftWall = edgeLeftWall.getRectangle();
                    Bounds boundRectangleEdgeLeftWall = rectangleEdgeLeftWall.getBoundsInParent();

                    Rectangle rectangleEdgeRightWall = edgeRightWall.getRectangle();
                    Bounds boundRectangleEdgeRightWall = rectangleEdgeRightWall.getBoundsInParent();

                    //----------COLLISIONS--------
                    //Collision with Floor/Roof
                    if (boundProjectileCircle.intersects(boundRectangleEdgeFloor) || boundProjectileCircle.intersects(boundRectangleEdgeRoof)) {
                        //We should check if the projectile has hit a portal before incrementing the counter because going through a portal should not count as a bounce
                        ++counterBounces;

                        if (boundProjectileCircle.intersects(boundRectangleEdgeFloor) && projectile.getType() == "portal") {
                            //TODO
                        }

                        if (boundProjectileCircle.intersects(boundRectangleEdgeRoof) && projectile.getType() == "portal") {
                            //TODO
                        }

                        if (boundProjectileCircle.intersects(boundRectangleEdgeFloor) && projectile.getType() == "antigravity") {
                            //TODO
                        }

                        if (boundProjectileCircle.intersects(boundRectangleEdgeRoof) && projectile.getType() == "portal") {
                            //TODO
                        }
                        //put if() statments to check for collision between antigravity and portals with the edges                    ---------TODO----------

                        //Moves the projectile one PROJECTILE_RADIUS away from the side it hits
                        if (boundProjectileCircle.intersects(boundRectangleEdgeFloor)) {
                            //Repeat this "if" statement but using portals and just add another check for if it's a portal projectile
                            tempProjectile.setPosition(new Vector(tempProjectile.getPosition().getX(), tempProjectile.getPosition().getY() - PROJECTILE_RADIUS));
                        }

                        //Moves the projectile one PROJECTILE_RADIUS away from the side it hits
                        if (boundProjectileCircle.intersects(boundRectangleEdgeRoof)) {
                            tempProjectile.setPosition(new Vector(tempProjectile.getPosition().getX(), tempProjectile.getPosition().getY() + PROJECTILE_RADIUS));
                        }

                        //This simulates the bouncing of the projectile by reversing it's velocity                       
                        tempProjectile.setVelocity(new Vector(tempProjectile.getVelocity().getX(), -tempProjectile.getVelocity().getY()));

                        if (counterBounces == MAX_NUMBER_OF_BOUNCES) {
                            counterBounces = 0;
                            objectList.remove(arrayListProjectiles.get(0));
                            arrayListProjectiles.remove(0);
                            removeFromPane(tempProjectile.getCircle());
                        }
                        //tempBounce.play(); --------------THE SOUND DOESNT WORK
                    } //Collision with Sides
                    else 
                    if (boundProjectileCircle.intersects(boundRectangleEdgeLeftWall) || boundProjectileCircle.intersects(boundRectangleEdgeRightWall)) {
                        ++counterBounces;
                        //AssetManager.getBounce().play();

                        //Moves the projectile one PROJECTILE_RADIUS away from the side it hits
                        if (boundProjectileCircle.intersects(boundRectangleEdgeLeftWall)) {
                            tempProjectile.setPosition(new Vector(tempProjectile.getPosition().getX() + PROJECTILE_RADIUS, tempProjectile.getPosition().getY()));
                        }

                        //Moves the projectile one PROJECTILE_RADIUS away from the side it hits
                        if (boundProjectileCircle.intersects(boundRectangleEdgeRightWall)) {
                            tempProjectile.setPosition(new Vector(tempProjectile.getPosition().getX() - PROJECTILE_RADIUS, tempProjectile.getPosition().getY()));
                        }

                        projectile.setVelocity(new Vector(-projectile.getVelocity().getX(), projectile.getVelocity().getY()));
                        if (counterBounces == MAX_NUMBER_OF_BOUNCES) {
                            counterBounces = 0;
                            objectList.remove(arrayListProjectiles.get(0));
                            arrayListProjectiles.remove(0);
                            removeFromPane(tempProjectile.getCircle());
                        }

                    }
                    projectile = tempProjectile;

                }//for (int i = 0; i < arrayListProjectiles.size(); i++)

            }//public void handle(long now)

        }.start();

    }

}

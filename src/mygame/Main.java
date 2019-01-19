package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.cinematic.Cinematic;
import com.jme3.cinematic.events.SoundEvent;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.water.SimpleWaterProcessor;
import com.jme3.water.WaterFilter;

public class Main extends SimpleApplication {
    private int bulletCount;
    private BitmapText hitText;
    private DirectionalLight light;
    private int winCount = 0;
    private int winSinCount = 0;
    AnimChannel danceChannel;
    AppState objectState = new AppState();
    
    private final static MouseButtonTrigger BUTTON_LEFT =
            new MouseButtonTrigger(MouseInput.BUTTON_LEFT);
    
    public BitmapText setsHudText(String shownText, ColorRGBA color){
        BitmapText text = new BitmapText(guiFont, false);
        text.setSize(guiFont.getCharSet().getRenderedSize());
        text.setColor(color);
        text.setText(shownText);
        return text;
    }
    //displays if winner
    public void displayStatus(){
        BitmapText healthText = setsHudText("Winner!!!", ColorRGBA.Red);           
        healthText.setLocalTranslation(0, 225, 0); 
        guiNode.attachChild(healthText);
    }
    //displays Health
    public void displayHealthHUD(){
        BitmapText healthText = setsHudText("Health: ", ColorRGBA.Red);           
        healthText.setLocalTranslation(0, 50, 0); 
        guiNode.attachChild(healthText);
    }
    //displays inventory
    public void displayInventoryHUD(){
        BitmapText inventoryText = setsHudText("Inventory: ", ColorRGBA.Red);           
        inventoryText.setLocalTranslation(0, 100, 0); 
        guiNode.attachChild(inventoryText);
    }
    //displays current Location
    public void displayCurrentLocationHUD(){
        BitmapText locationText = setsHudText("Current Location: ", ColorRGBA.Red);           
        locationText.setLocalTranslation(0, 150, 0); 
        guiNode.attachChild(locationText);
    }
    //displays hit count
    public void displayHitCountHUD(){
        bulletCount = 0;
        hitText = setsHudText("Hits: "+ bulletCount, ColorRGBA.Red);
        hitText.setLocalTranslation(0, 200, 0); 
        guiNode.attachChild(hitText);
    }
    //shows the crosshairs
    public void displayCrosshairsHUD(){
        BitmapText hudText = new BitmapText(guiFont, false);
        hudText.setColor(ColorRGBA.Red);                             
        hudText.setText("+");             
        hudText.setLocalTranslation(313, 252, 0); 
        guiNode.attachChild(hudText);
    }
    //Creates the bullet
    private Geometry addHit(Vector3f location){
       Sphere bullet = new Sphere(32,32,0.3f);
       Geometry hit = new Geometry("Bullet", bullet);
       Material mat = new Material (assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
       mat.setColor("Color", ColorRGBA.Orange);
       mat.getAdditionalRenderState().setWireframe(true);
       hit.setMaterial(mat);
       hit.setLocalTranslation(location);
       ++bulletCount;
       return hit;
   }
    //where the bullet connects to an object
    private void addCollision(Spatial s) {
        CollisionResults results = new CollisionResults();
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        s.collideWith(ray, results);
        
        if (results.size() >0){
            CollisionResult closest = results.getClosestCollision();
            Vector3f firstHit = closest.getContactPoint();
            //attaches bullet to object
            closest.getGeometry().getParent().attachChild(addHit(closest.getGeometry().getParent().worldToLocal(firstHit, firstHit)));
            //if bullet hits the 'monster' node it adds one to the count to determine if they won
            if (closest.getGeometry().getParent() == objectState.getmonster()){
                winCount++;
                if (winCount == 10){
                    objectState.getShootables().detachChild(objectState.getmonster());
                    displayStatus();
                }
            }
            
            else if (closest.getGeometry().getParent().getParent() == objectState.getSinbad()){
                winSinCount++;
                if (winSinCount == 5){
                    objectState.getShootables().detachChild(objectState.getSinbad());
                    
                }  
            }
        }
        
    }
    //add the collision and play the situational sound after the bullet connected to object
    private final ActionListener listener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if ("Shoot".equals(name)) 
                    if(!keyPressed){
                       addCollision(objectState.getShootables()); 
                       AudioNode gunSound = new AudioNode(assetManager, "Sound/Effects/Gun.wav", DataType.Buffer);
                       gunSound.setLooping(false);
                       gunSound.setPositional(false);
                       gunSound.setVolume(3f);
                       objectState.getShootables().attachChild(gunSound);
                       gunSound.play();
                    }
            //if (name.equals("Dance") && !keyPressed) {
              //  danceChannel.setAnim("Dance", 0.50f);
            //}
            }
            
           
        
    };
    //background noise 
    private void initAudio() {
        AudioNode background = new AudioNode(assetManager, 
                "Sound/Environment/Ocean Waves.ogg", true);
        background.setLooping(true);
        background.setPositional(true);
        background.setVolume(1);
        
        rootNode.attachChild(background);
        //background.play(); // play continuously!
    }
    //setup mapping and listener for the 'Shoot'
    private void initKeys() {
        inputManager.addMapping("Shoot",
            new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(listener, "Shoot");
        inputManager.addMapping("Dance",
        new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(listener, "Dance");
    }
    //building the HUD
    public void buildHUD(){
        displayHealthHUD();
        displayInventoryHUD();
        displayCurrentLocationHUD();
        displayHitCountHUD();
        displayCrosshairsHUD();
    }
     public static void main(String[] args) {
        Main app = new Main();
        app.setDisplayFps(false);
        app.setDisplayStatView(false);
        app.setShowSettings(false);
        app.start();
    }
    //sets up the shadows and light
    public void light(){
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.25f));
        rootNode.addLight(ambient);
        light = new DirectionalLight();
        light.setDirection(new Vector3f(0, -1, -1));
        light.setColor(ColorRGBA.White.mult(0.25f));
        
        DirectionalLightShadowRenderer shadow = 
                new DirectionalLightShadowRenderer(assetManager, 512,2);
        shadow.setLight(light);
        viewPort.addProcessor(shadow);
        rootNode.addLight(light);
    }
    @Override
    public void simpleInitApp() {
        Vector3f defCam = new Vector3f(0, 15f,0);
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(40);
        cam.setLocation(defCam);
        stateManager.attach(objectState);
        buildHUD();
        initAudio();
        light();
        //picture in picture 
        Camera cam2 = cam.clone();
        cam2.setViewPort(0.75f, 0.95f, 0.7f, 0.9f);
        cam2.setLocation(new Vector3f(0, 45, 0));
        cam2.lookAtDirection(new Vector3f(0, -1, 0), Vector3f.UNIT_X);
        initKeys();

        Spatial sky = SkyFactory.createSky(assetManager,
                "Textures/Sky/Bright/BrightSky.dds", false);
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        WaterFilter water = new WaterFilter(rootNode, new Vector3f(-1,0,0));
        water.setWaterHeight(1.0f);
        fpp.addFilter(water);
        viewPort.addProcessor(fpp);
        rootNode.attachChild(sky);

    }
    
    @Override
    public void simpleUpdate(float tpf) {
        //updates the hitcount
        
        hitText.setText("Hit Count: " + bulletCount);
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}

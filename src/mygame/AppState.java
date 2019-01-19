package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.cinematic.Cinematic;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.cinematic.events.SoundEvent;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Dome;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.TangentBinormalGenerator;


public class AppState extends AbstractAppState implements AnimEventListener{
    private SimpleApplication app;
    private AssetManager assetManager;
    private InputManager inputManager;
    private Node spinningSphereNode = new Node ("Spinning Node");
    private Node Shootables = new Node("Shootables");
    private Node Sinbad = new Node("SinBad");
    private Node monster = new Node("Monster");
    private Camera cam;
    AnimControl control;
    AnimChannel danceChannel;
    public Geometry sphereGeometry1;
    private final static MouseButtonTrigger BUTTON_LEFT =
            new MouseButtonTrigger(MouseInput.BUTTON_LEFT);
    
    
    public Node getSpinningSphereNode() {
        return spinningSphereNode;
    }

    public void setSpinningSphereNode(Node spinningSphereNode) {
        this.spinningSphereNode = spinningSphereNode;
    }
    public Node getmonster() {
        return monster;
    }

    public void setmonster(Node monster) {
        this.monster = monster;
    }
    public Node getShootables() {
        return Shootables;
    }

    public void setShootables(Node Shootables) {
        this.Shootables = Shootables;
    }
    public Node getSinbad() {
        return Sinbad;
    }

    public void setSinbad(Node Sinbad) {
        this.Sinbad= Sinbad;
    }
    @Override
    public void cleanup() {
        super.cleanup(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update(float tpf) {
        super.update(tpf); //To change body of generated methods, choose Tools | Templates.
        spinningSphereNode.rotate(0,.001f,0);

    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled); //To change body of generated methods, choose Tools | Templates.
    }
    
        
    @Override
    public void initialize(
            AppStateManager stateManager,
            Application app) {
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        this.assetManager = app.getAssetManager();
        this.inputManager = app.getInputManager();
        this.cam = app.getCamera();

        //load in the blender
        //Spatial deer = assetManager.loadModel(
          //  "deerBlend/deer.j3o");
        //deer.setLocalTranslation(-15,0,0);
        //attach the blender to the rootNode
        //this.app.getRootNode().attachChild(deer);
        //attach the spinning sphere to the spining sphere
        Spatial sinbad = assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
        sinbad.setLocalTranslation(0, 17f,0);
        control = sinbad.getControl(AnimControl.class);
        control.addListener(this);
        danceChannel = control.createChannel();
        danceChannel.setAnim("Dance");
        spinningSphereNode.attachChild(getTargetSphere());
        
        MotionPath path = new MotionPath();
        
        path.addWayPoint(new Vector3f(5, 17f, 0));
        path.addWayPoint(new Vector3f(-5,17f,0));
        path.addWayPoint(new Vector3f(-10, 17f, 0));
        path.addWayPoint(new Vector3f(0, 17f, -5));
        path.addWayPoint(new Vector3f(-10, 17f, 0));
        path.addWayPoint(new Vector3f(0, 17f, 0));
        path.setCurveTension(1.0f);
        path.setCycle(true);
        
        MotionEvent motionEvent = new MotionEvent(sinbad, path);
        motionEvent.play();
        motionEvent.setLoopMode(LoopMode.Loop);
        
        Cinematic cin = new Cinematic(Sinbad);
        stateManager.attach(cin);
        cin.addCinematicEvent(3, motionEvent);
        cin.play();
        
       

        Spatial terrain = assetManager.loadModel("Textures/terrain-final.j3o");
        
        terrain.setLocalTranslation(0, 2, 0);
        
        SoundEvent soundEvent = new SoundEvent("Sound/Effects/Foot steps.ogg",LoopMode.Loop);
        cin.addCinematicEvent(0, soundEvent);
        cin.play();
        
        
        Shootables.attachChild(spinningSphereNode);
        Shootables.attachChild(getSphere());
        Shootables.attachChild(getSphere2());
        Shootables.attachChild(terrain);
        //Shootables.attachChild(getBox());
        //Shootables.attachChild(specialSphere());
        //build the monster
        attachMonster();
        Shootables.attachChild(monster);
        Sinbad.attachChild(sinbad);
        Shootables.attachChild(Sinbad);
        this.app.getRootNode().attachChild(Shootables);
        //this.app.getRootNode().attachChild(makeFloor());
        
        
    }
    //builds the monster
    private void attachMonster(){
        monster.attachChild(body());
        monster.attachChild(head());
        monster.attachChild(arm1());
        monster.attachChild(arm2());
        monster.attachChild(eye1());
        monster.attachChild(eye2());
        monster.attachChild(mouth());
        monster.attachChild(leg1());
        monster.attachChild(leg2());
        monster.setLocalTranslation(-5, 15f,0);
        monster.setShadowMode(ShadowMode.CastAndReceive);
    }

    private Geometry getSphere(){
        Sphere sphere = new Sphere(10,10,1f);
        Geometry sphereGeometry = new Geometry ("Sphere", sphere);
        TangentBinormalGenerator.generate(sphere);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        sphereGeometry.setLocalTranslation(18, 15f,0);
        mat.setBoolean("UseMaterialColors",true);
        mat.setColor("Diffuse",ColorRGBA.Red);  
        mat.setColor("Specular",ColorRGBA.White); 
        mat.setColor("Ambient", ColorRGBA.Gray);
        sphereGeometry.setMaterial(mat);
        sphereGeometry.setShadowMode(ShadowMode.CastAndReceive);
        return sphereGeometry;
        
    }

    private Geometry getTargetSphere(){
        Sphere target = new Sphere(15,15,2f);
        sphereGeometry1 = new Geometry ("Target", target);
        target.setTextureMode(Sphere.TextureMode.Projected);
        TangentBinormalGenerator.generate(target);
        Material sphereMat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        sphereGeometry1.setLocalTranslation(0,30f,0);
        sphereMat.setBoolean("UseMaterialColors",true);
        sphereMat.setColor("Diffuse",ColorRGBA.Red);  
        sphereMat.setColor("Specular",ColorRGBA.Orange); 
        sphereMat.setColor("Ambient", ColorRGBA.Gray);
        sphereMat.setFloat("Shininess", 64f);
        sphereGeometry1.setMaterial(sphereMat);
        sphereGeometry1.setShadowMode(ShadowMode.CastAndReceive);
        return sphereGeometry1;
    }
    /*
    private Geometry getBox(){
        Box box = new Box(5, 5, 5);
        Geometry BoxGeometry = new Geometry ("Box", box);
        TangentBinormalGenerator.generate(box);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap",
        assetManager.loadTexture("Textures/grass.jpg"));
        mat.setTexture("NormalMap",
        assetManager.loadTexture("Textures/grass.jpg"));
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        BoxGeometry.setLocalTranslation(19,5,3);
        mat.setBoolean("UseMaterialColors",true);
        mat.setColor("Diffuse",ColorRGBA.Red);  
        mat.setColor("Specular",ColorRGBA.White); 
        mat.setColor("Ambient", ColorRGBA.Gray);
        BoxGeometry.setMaterial(mat);
        BoxGeometry.setShadowMode(ShadowMode.CastAndReceive);
        return BoxGeometry;
    }
    */
    private Geometry getSphere2(){
        Sphere sphere = new Sphere(10,10,1f);
        Geometry sphereGeometry = new Geometry ("Sphere2", sphere);
        TangentBinormalGenerator.generate(sphere);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap",
        assetManager.loadTexture("Textures/pebble.jpg"));
        mat.setTexture("NormalMap",
        assetManager.loadTexture("Textures/pebble.jpg"));
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        sphereGeometry.setLocalTranslation(25, 15f, 0);
        mat.setBoolean("UseMaterialColors",true);
        mat.setColor("Diffuse",ColorRGBA.White);  
        mat.setColor("Specular",ColorRGBA.Gray); 
        mat.setColor("Ambient", ColorRGBA.Gray);
        sphereGeometry.setMaterial(mat);
        sphereGeometry.setShadowMode(ShadowMode.CastAndReceive);
        return sphereGeometry;
    }
    /*
    protected Geometry makeFloor() {
        Box box = new Box(100, .15f, 100);
        Geometry floor = new Geometry("the Floor", box);
        floor.setLocalTranslation(0, 3, 0);
        Material mat1 = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        mat1.setTexture("DiffuseMap",
        assetManager.loadTexture("Textures/grass.jpg"));
        mat1.setTexture("NormalMap",
        assetManager.loadTexture("Textures/grass.jpg"));
        mat1.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        floor.setMaterial(mat1);
        floor.setShadowMode(ShadowMode.Receive);
        return floor;
    }
    */
    public Geometry body(){
        Box b = new Box(3, 1.5f, 1);
        Geometry geom = new Geometry("Body", b);
        geom.setLocalTranslation(0,0,0);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        return geom;
    
    }

    public Geometry arm1(){
        Box b = new Box(.5f, 1f, 1);
        Geometry geom = new Geometry("Box", b);
        geom.setLocalTranslation(3.5f,.5f,0);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        geom.setMaterial(mat);
        return geom;
    }
    public Geometry arm2(){
        Box b = new Box(.5f, 1f, 1);
        Geometry geom = new Geometry("Box", b);
        geom.setLocalTranslation(-3.5f,.5f,0);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        geom.setMaterial(mat);
        return geom;
    }
    public Geometry head(){
        Sphere s = new Sphere(12,12,1f);
        Geometry geom = new Geometry("Sphere", s);
        geom.setLocalTranslation(0,2.5f,0);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        mat.setColor("Color", ColorRGBA.White);
        geom.setMaterial(mat);
        return geom;
    
    }
    public Geometry eye1(){
        Sphere s = new Sphere(3,7,.2f);
        Geometry geom = new Geometry("Sphere", s);
        geom.setLocalTranslation(.75f,2.75f,.5f);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Black);
        geom.setMaterial(mat);
        return geom;
    
    }
    public Geometry eye2(){
        Sphere s = new Sphere(3,7,.2f);
        Geometry geom = new Geometry("Sphere", s);
        geom.setLocalTranslation(-.75f,2.75f,.5f);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Black);
        geom.setMaterial(mat);
        return geom;
    }
    public Geometry mouth(){
        Dome d = new Dome(5,7,.4f);
        Geometry geom = new Geometry("Dome", d);
        geom.setLocalTranslation(0,2f,1.3f);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Black);
        geom.setMaterial(mat);
        return geom;
    }
    public Geometry leg1(){
        Box b = new Box(.25f,.75f,0);
        Geometry geom = new Geometry("Box", b);
        geom.setLocalTranslation(.8f,-1.8f,0);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        geom.setMaterial(mat);
        return geom;
    }
    public Geometry leg2(){
        Box b = new Box(.25f,.75f,0);
        Geometry geom = new Geometry("Box", b);
        geom.setLocalTranslation(-.8f,-1.8f,0);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        geom.setMaterial(mat);
        return geom;
    }
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        if (animName.equals("Dance")) {
            channel.setAnim("IdleBase");
        }
    }
    public void onAnimChange(AnimControl control,AnimChannel channel, String animName) {
        if (animName.equals("IdleBase")) {
            channel.setAnim("Dance");
        }
    }
    /*
    public Geometry specialSphere(){
        Sphere s = new Sphere(12,12,1f);
        Geometry geom = new Geometry("Sphere", s);
        geom.setLocalTranslation(5,10,0);
        geom.setMaterial(assetManager.loadMaterial("Materials/sphereMaterial.j3m"));
        geom.setShadowMode(ShadowMode.CastAndReceive);
        return geom;
    
    }
    */

}

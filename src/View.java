import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


/**
 * Created by ashesh on 9/18/2015.
 *
 * The View class is the "controller" of all our OpenGL stuff. It cleanly encapsulates all our OpenGL functionality from the rest of Java GUI, managed
 * by the JOGLFrame class.
 */
public class View
{
    private int WINDOW_WIDTH,WINDOW_HEIGHT;
    private Stack<Matrix4f> modelView;
    private Matrix4f projection,trackballTransform;
    private float trackballRadius, framecount;
    private Vector2f mousePos;


    private util.ShaderProgram program;
    private util.ShaderLocationsVault shaderLocations;
    private int projectionLocation;
    private sgraph.IScenegraph<VertexAttrib> scenegraph;

    private String cameraMode;
    private float helicopterX, helicopterZ;
    private Matrix4f walkingYRotate, walkingRotate, walkingTranslate;
    private Vector4f walkingDirection;

    AWTGLReadBufferUtil screenCaptureUtil;

    public View()
    {
        projection = new Matrix4f();
        modelView = new Stack<Matrix4f>();
        trackballRadius = 300;
        trackballTransform = new Matrix4f();
        scenegraph = null;
        cameraMode = "global";
        helicopterX = 0;
        helicopterZ = 0;
        walkingYRotate = new Matrix4f();
        walkingRotate = new Matrix4f();
        walkingTranslate = new Matrix4f();
        walkingDirection = new Vector4f(1,0,0,0);
        screenCaptureUtil = null;
    }

    public void updateCameraMode(String mode) {
        cameraMode = mode;

        if (mode.equals("helicopter")) {
            helicopterX = 0;
            helicopterZ = 0;
        } else if (mode.equals("global")) {
            trackballTransform.identity();
        } else if (mode.equals("walking")) {
            walkingYRotate.identity();
            walkingRotate.identity();
            walkingTranslate.identity();
            walkingDirection = new Vector4f(1,0,0,0);
        }
    }

    public void updateCamera(float change, String coord) {
        if (cameraMode.equals("helicopter")) {
            int finalRadius = 15;

            if (coord.equals("s")) {
                float newX = helicopterX + change;
                double radius = Math.sqrt((newX * newX) + (helicopterZ * helicopterZ));

                if (radius > finalRadius) {
                    helicopterX = (float) Math.sqrt((finalRadius * finalRadius) - (helicopterZ * helicopterZ));
                    if (change < 0) {
                        helicopterX = -1 * helicopterX;
                    }
                } else {
                    helicopterX = newX;
                }
            } else if (coord.equals("t")) {
                float newZ = helicopterZ + change;
                double radius = Math.sqrt((newZ * newZ) + (helicopterX * helicopterX));

                if (radius > finalRadius) {
                    helicopterZ = (float) Math.sqrt((finalRadius * finalRadius) - (helicopterX * helicopterX));
                    if (change < 0) {
                        helicopterZ = -1 * helicopterZ;
                    }
                } else {
                    helicopterZ = newZ;
                }
            }
        } else if (cameraMode.equals("walking")) {
            if (coord.equals("s")) {
                walkingYRotate.rotate((float) Math.toRadians(change*-2),0,1,0);
            } else if (coord.equals("t")) {
                walkingTranslate.translate(-2*change*walkingDirection.x,-2*change*walkingDirection.y,-2*change*walkingDirection.z);
            } else if (coord.equals("w")) {
                walkingTranslate.translate(0,-2*change,0);
            } else if (coord.equals("a")){
                walkingRotate.rotate((float) Math.toRadians(change*-2),1,0,0);
            }
        }

    }

    public void initScenegraph(GLAutoDrawable gla,InputStream in) throws Exception
    {
        GL3 gl = gla.getGL().getGL3();

        if (scenegraph!=null)
            scenegraph.dispose();

        program.enable(gl);

        scenegraph = sgraph.SceneXMLReader.importScenegraph(in,new VertexAttribProducer());

        sgraph.IScenegraphRenderer renderer = new sgraph.GL3ScenegraphRenderer();
        renderer.setContext(gla);
        Map<String,String> shaderVarsToVertexAttribs = new HashMap<String,String>();
        shaderVarsToVertexAttribs.put("vPosition","position");
        shaderVarsToVertexAttribs.put("vNormal","normal");
        shaderVarsToVertexAttribs.put("vTexCoord","texcoord");
        renderer.initShaderProgram(program,shaderVarsToVertexAttribs);
        scenegraph.setRenderer(renderer);
        program.disable(gl);
    }

    public void init(GLAutoDrawable gla) throws Exception
    {
        GL3 gl = gla.getGL().getGL3();

        //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
        program = new util.ShaderProgram();

        program.createProgram(gl,"shaders/phong-multiple.vert","shaders/phong-multiple.frag");

        shaderLocations = program.getAllShaderVariables(gl);

        //get input variables that need to be given to the shader program
        projectionLocation = shaderLocations.getLocation("projection");
    }



    public void draw(GLAutoDrawable gla)
    {
        GL3 gl = gla.getGL().getGL3();

        gl.glClearColor(0.85f,0.93f,1,1);
        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);

        program.enable(gl);

        while (!modelView.empty())
            modelView.pop();

        /*
         *In order to change the shape of this triangle, we can either move the vertex positions above, or "transform" them
         * We use a modelview matrix to store the transformations to be applied to our triangle.
         * Right now this matrix is identity, which means "no transformations"
         */
        modelView.push(new Matrix4f().identity());

        framecount++;
        scenegraph.animate(framecount);

        if (cameraMode == "global") {
            modelView.peek().lookAt(new Vector3f(0,400,400),new Vector3f(0,0,0),new Vector3f(0,1,0))
                    .mul(trackballTransform);
        } else if (cameraMode == "far-bike") {
            Matrix4f handleToWorld = new Matrix4f()
                    .rotate((float) Math.toRadians(framecount), 0, 1, 0)
                    .translate(400,0,0)
                    .rotate((float) Math.toRadians(180),0,1,0)
                    .scale(250, 250, 250)
                    .scale(0.008f, 0.008f, 0.008f)
                    .translate(0, 50, 0);
            Matrix4f worldToHandle = handleToWorld.invert();

            Matrix4f HandleToView = new Matrix4f().lookAt(
                    new Vector3f(0, 0, 0),
                    new Vector3f(0, 0, 10),
                    new Vector3f(0, 1, 0));
            Matrix4f worldToView = new Matrix4f(HandleToView.mul(worldToHandle));
            modelView.peek().lookAt(
              new Vector3f(0, 0, 0),
              new Vector3f(0, 0, 10),
              new Vector3f(0, 1, 0)).mul(worldToHandle);
        } else if (cameraMode == "near-bike") {
            Matrix4f handleToWorld = new Matrix4f()
                    .rotate((float) Math.toRadians(-2*framecount), 0, 1, 0)
                    .translate(200,0,0)
                    .scale(250, 250, 250)
                    .scale(0.008f, 0.008f, 0.008f)
                    .translate(0, 50, 0);
            Matrix4f worldToHandle = handleToWorld.invert();

            Matrix4f HandleToView = new Matrix4f().lookAt(
                    new Vector3f(0, 0, 0),
                    new Vector3f(0, 0, 10),
                    new Vector3f(0, 1, 0));
            Matrix4f worldToView = new Matrix4f(HandleToView.mul(worldToHandle));
            modelView.peek().mul(worldToView);
        } else if (cameraMode == "helicopter") {
            Matrix4f helicopterToWorld = new Matrix4f()
                    .rotate((float) Math.toRadians(-framecount/2), 0, 1, 0)
                    .translate(300,300,0)
                    .scale(250, 250, 250)
                    .scale(0.008f, 0.008f, 0.008f)
                    .translate(0, 0, 0);
            Matrix4f worldToHelicopter = helicopterToWorld.invert();

            Matrix4f HelicopterToView = new Matrix4f().lookAt(
                    new Vector3f(0, 0, 0),
                    new Vector3f(helicopterX, -10, helicopterZ),
                    new Vector3f(0, 0, 1));
            Matrix4f worldToView = new Matrix4f(HelicopterToView.mul(worldToHelicopter));
            modelView.peek().mul(worldToView);
        } else if (cameraMode == "walking") {
            modelView.peek().lookAt(new Vector3f(0,100,0),new Vector3f(100,100,0),new Vector3f(0,1,0))
                    .mul(walkingTranslate).mul(walkingYRotate).mul(walkingRotate);
        }


    /*
     *Supply the shader with all the matrices it expects.
    */
        FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
        gl.glUniformMatrix4fv(projectionLocation,1,false,projection.get(fb));
        //return;


        //gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL3.GL_LINE); //OUTLINES

        scenegraph.draw(modelView);
    /*
     *OpenGL batch-processes all its OpenGL commands.
          *  *The next command asks OpenGL to "empty" its batch of issued commands, i.e. draw
     *
     *This a non-blocking function. That is, it will signal OpenGL to draw, but won't wait for it to
     *finish drawing.
     *
     *If you would like OpenGL to start drawing and wait until it is done, call glFinish() instead.
     */
        gl.glFlush();

        program.disable(gl);



    }

    public void mousePressed(int x,int y)
    {
        mousePos = new Vector2f(x,y);
    }

    public void mouseReleased(int x,int y)
    {
        System.out.println("Released");
    }

    public void mouseDragged(int x,int y)
    {
        if (cameraMode != "global") {
            return;
        }

        Vector2f newM = new Vector2f(x,y);

        Vector2f delta = new Vector2f(newM.x-mousePos.x,newM.y-mousePos.y);
        mousePos = new Vector2f(newM);

        trackballTransform = new Matrix4f().rotate(delta.x/trackballRadius,0,1,0)
                                           .rotate(delta.y/trackballRadius,1,0,0)
                                           .mul(trackballTransform);
    }

    public void reshape(GLAutoDrawable gla,int x,int y,int width,int height)
    {
        GL gl = gla.getGL();
        WINDOW_WIDTH = width;
        WINDOW_HEIGHT = height;
        gl.glViewport(0, 0, width, height);

        projection = new Matrix4f().perspective((float)Math.toRadians(120.0f),(float)width/height,0.1f,10000.0f);
        //projection = new Matrix4f().ortho(-50, 50, -50, 50, 0.1f, 10000.0f);

    }

    public void dispose(GLAutoDrawable gla)
    {
        GL3 gl = gla.getGL().getGL3();

    }

    public void captureFrame(String filename,GLAutoDrawable gla) throws
      FileNotFoundException,IOException {
        if (screenCaptureUtil==null) {
            screenCaptureUtil = new AWTGLReadBufferUtil(gla.getGLProfile(),false);
        }

        File f = new File(filename);
        GL3 gl = gla.getGL().getGL3();

        BufferedImage image = screenCaptureUtil.readPixelsToBufferedImage(gl,true);
        OutputStream file = null;
        file = new FileOutputStream(filename);

        ImageIO.write(image,"png",file);

    }

}

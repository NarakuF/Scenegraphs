Guandong Liu
Katherine McDonough

Assignment Four

Part One:
Lighting Basics - Scenegraphs can now store and parse multiple lights. Each object can now have a material with ambient, diffuse, material, and shininess values. Incorporated lighting shaders into the rendering of the scene. Wrote functions to acquire all of the lights in the view coordinate system. The scenegraph will obtain all lights in the view coordinate system, pass the lights to the shader, and draw the scene. The drawing will also pass all material elements instead of just “color.”

Spot Light - The XML parser has the capability for spot direction and angle then pass to an updated shader for viewing a spotlight. 

Texture Mapping - The XML parser has the capability to read textures from multiple xml files and apply them to the marked objects. 

Part Two:
Helicopter - Added a second model that is also animated

Ground Plane - Added a box to act as the ground

Materials - Updated all the colors to be materials instead

Textures - Added a texture to the helicopter cockpit and the box that is the ground
Created cockpit texture myself. Grass texture can be found: http://allergistlafayette.com/site/allergy/air-borne-allergens/grass-allergy/

Lights - Added a stationary light to the whole scene, added a spot light from the bottom of the helicopter, and added spotlights to the front of the bicycles to act as lights

Part Three:
Pressing “G” is the global view with trackball implementation. 

Pressing “O” lets you view as if riding the inside bike and “F” lets you view as riding the outside bike.

Pressing “H” lets you view from the bottom of the helicopter and the arrow keys allow you to change the angles.

Pressing “I” lets you view as a person walking around the scene. Specific use can be found in the Documentation document. 

Extra Credit:
“I” and “H” camera are both key-board controlled. 
“G” is mouse controlled.
Bench model was added and two instances can be found in the final scene. 

Assignment Three

Part 2A:
We created two XML files, humanoid-Y.xml and humanoid-A.xml, to show the humanoid in the Y and A positions of the YMCA dance respectively. We created a third XML file, humanoid-YMCA.xml, that positions the two humanoids in their positions side by side.

Part 2B:
We created an XML file, bicycle.xml, that incorporates all of the elements required to draw a bicycle in the scenegraph format, including group, transformation, and object nodes. The nodes are appropriately named according to the part of the bicycle. We created an additional XML file, two-bicycles.xml, that positions the bicycles side by side facing opposite directions. 

Part 2C:
We animated the bicycles to move in two circles in opposite directions, rotate the bicycle wheels around their respective centers, and rotate the pedal gears so the pedals appear to be rotating as if someone is riding the bicycle. The two bicycles can be seen from the stationary camera view located at (0,100,275) and looking at point (0,0,0). When initializing the JOGL frame for the scene the two-bicycles.xml file is loaded via an InputStream.

Documentation includes more detailed information.
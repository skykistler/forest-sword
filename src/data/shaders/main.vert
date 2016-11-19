uniform float lightX;
uniform float lightY;
uniform float lightZ;

varying vec4 position;
varying vec4 color;
varying vec3 normal;
varying vec2 texCoord;
varying vec3 lightDir;
varying vec3 eyeVec;

void main()
{
   gl_Position = position = gl_ModelViewProjectionMatrix * gl_Vertex;
   color = gl_Color;
   normal = normalize(gl_NormalMatrix * gl_Normal);
   gl_TexCoord[0] = gl_MultiTexCoord0;
   texCoord = gl_TexCoord[0].xy;

   vec3 lightPos = vec3(lightX, lightY, lightZ);

   lightDir = vec3(lightPos - position.xyz);
   eyeVec = -position.xyz;
}
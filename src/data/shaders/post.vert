varying vec4 position;
varying vec4 color;
varying vec3 normal;
varying vec2 texCoord;

void main()
{
   gl_Position = position = gl_ModelViewProjectionMatrix * gl_Vertex;
   color = gl_Color;
   normal = normalize(gl_NormalMatrix * gl_Normal);
   gl_TexCoord[0] = gl_MultiTexCoord0;
   texCoord = gl_TexCoord[0].xy;
}
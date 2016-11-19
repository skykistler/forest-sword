uniform vec2 cameraNearAndFar;
uniform vec2 displayDimensions;
uniform vec3 skyLight;
uniform int lantern;
uniform float timeMod;
uniform sampler2D tex;
uniform float renderDistance;
uniform int fog;
 
varying vec2 texCoord;
varying vec4 position;
varying vec4 color;
varying vec3 normal;
varying vec3 lightDir;
varying vec3 eyeVec;

void main()
{	
    float depth = gl_FragCoord.z / gl_FragCoord.w / renderDistance;
    vec4 col1 = color;
    
   	if(depth > 0.0 && lantern == 1) {
   	  float z2 = 1.0 / (depth * 5.0);
   	  z2 = clamp(z2, 0.0, 4.0);
   	  vec3 col2 = col1.xyz * z2;
   	  col1.xyz = max(col1.xyz, col2);
   	}
   	
	vec4 texcol = texture2D(tex, texCoord.xy);
	//if(textureSize(tex, 0).x > 0)
	  col1 = col1 * texcol;

	col1.xyz *= max(max(skyLight.x, skyLight.y), skyLight.z);
	
	vec4 withoutLight = color * texcol;
	if(col1.x > withoutLight.x * 0.9){
	  col1.x = withoutLight.x * 0.9;
	}  
	if(col1.y > withoutLight.y * 0.9){
	  col1.y = withoutLight.y * 0.9;
	}  
	if(col1.z > withoutLight.z * 0.9){
	  col1.z = withoutLight.z * 0.9;
	} 
	
	if(fog == 1) {
      float fog= exp2(-gl_Fog.density * gl_Fog.density * depth * depth * 1.4427);
      fog = clamp(fog, 0.0, 1.0);
   	  col1.xyz = mix(gl_Fog.color, col1, fog).xyz;
   	}
    
    float max = max(col1.x, max(col1.y, col1.z));
    float s = .7 - (timeMod + 1.0) / 2.0;
    if(s > .7)
   	  s = .7;
   	if(s < 0.0)
   	  s= 0.0;
    gl_FragColor.x = col1.x + s * (max - col1.x);
    if(gl_FragColor.x == col1.x && texcol.x - texcol.y > .25)
      s = .2;
    gl_FragColor.y = col1.y + s * (max - col1.y);
    gl_FragColor.z = col1.z + s * (max - col1.z);
    gl_FragColor.w = col1.w;
}
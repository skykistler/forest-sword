uniform vec2 cameraNearAndFar;
uniform vec2 displayDimensions;
uniform sampler2D tex;
uniform sampler2D depthTex;
uniform float timeMod;
 
varying vec2 texCoord;
varying vec4 position;
varying vec4 color;
varying vec3 normal;

void main()
{	
    vec4 col1 = color * texture2D(tex, texCoord.xy);
	
    float blurSamples = 0.0;
    float blurWidth = 1.0 / displayDimensions.x;
    float blurHeight = 1.0 / displayDimensions.y;
	if(blurSamples > 0.0){
      vec4 sum = vec4( 0.0 );
      for (float x = -4.0; x <= 4.0; x++)
          for (float y = -4.0; y <= 4.0; y++)
              sum += texture2D(tex, vec2(texCoord.x + x * blurWidth, texCoord.y + y * blurHeight)) / blurSamples;
      col1 = sum;
    }
    
    gl_FragColor = col1;
}
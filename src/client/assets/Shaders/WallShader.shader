// Upgrade NOTE: replaced 'SeperateSpecular' with 'SeparateSpecular'

Shader "Wall Vertex Colored" {
Properties {
    _Color ("Main Color", Color) = (1,1,1,1)
    _SpecColor ("Spec Color", Color) = (1,1,1,1)
    _Emission ("Emmisive Color", Color) = (0,0,0,0)
    _Shininess ("Shininess", Range (0.01, 1)) = 0.7
    _MainTex ("Base (RGB)", 2D) = "white" {}
	_Cutoff ("Alpha cutoff", Range(0,1)) = 0.5
}

SubShader {
	
  //  Pass {

        Material {
            Shininess [_Shininess]
            Specular [_SpecColor]
            Emission [_Emission]    
        }

		Tags { "Queue"="AlphaTest" "IgnoreProjector"="True" "RenderType"="TransparentCutout"}

		Cull Off
        ColorMaterial AmbientAndDiffuse
        Lighting On
        SeparateSpecular On

		

//        SetTexture [_MainTex] {
//            Combine texture * primary, texture * primary
//        }
//        SetTexture [_MainTex] {
//            constantColor [_Color]
//            Combine previous * constant DOUBLE, previous * constant
//        } 

		CGPROGRAM
		#pragma surface surf Lambert alphatest:_Cutoff

		sampler2D _MainTex;
		fixed4 _Color;

		struct Input {
			float2 uv_MainTex;
		};

		void surf(Input IN, inout SurfaceOutput o) {
			fixed4 c = tex2D(_MainTex, IN.uv_MainTex) * _Color;
			o.Albedo = c.rgb;
			o.Alpha = c.a;
		}

		ENDCG
		
    // }
}

Fallback " VertexLit", 1
}
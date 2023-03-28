precision mediump float;
uniform sampler2D u_TextureUnit;
uniform sampler2D u_TextureUnit2;
varying vec2 v_TextureCoordinates;

void main() {
    gl_FragColor = (0.7*texture2D(u_TextureUnit,v_TextureCoordinates))+
    (0.3*texture2D(u_TextureUnit2,v_TextureCoordinates));

}
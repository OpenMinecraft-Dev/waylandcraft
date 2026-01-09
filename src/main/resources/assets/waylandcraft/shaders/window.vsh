#version 430 core

layout(location = 0) in vec2 pos;
layout(location = 1) in vec2 uv;

out vec2 texCoord;
uniform mat4 transform;

void main() {
	gl_Position = transform * vec4(pos, 0.0f, 1.0f);
	texCoord = uv;
}

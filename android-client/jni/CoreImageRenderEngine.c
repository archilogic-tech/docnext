#include <jni.h>
#include <GLES/gl.h>
#include <GLES/glext.h>
#include <android/log.h>

void Java_jp_archilogic_docnext_android_coreview_image_CoreImageRenderEngine_drawSingleImageJNI
(JNIEnv * env, jobject obj, jint id, jfloat x, jfloat y, jfloat w, jfloat h) {
  // __android_log_write(ANDROID_LOG_DEBUG,"Tag","Message");

  glBindTexture( GL_TEXTURE_2D , id );
  glDrawTexfOES( x , y , 0 , w , h );
}

#include <jni.h>
#include <GLES/gl.h>
#include <GLES/glext.h>
#include <math.h>
#include <android/log.h>
#include "CoreImageRenderEngine.h"

#define TEXTURE_SIZE jp_archilogic_docnext_android_coreview_image_CoreImageRenderEngine_TEXTURE_SIZE

float mat_len( float length , float scale ) {
  return length * scale;
}

float mat_x( float x , float scale , float tx ) {
  return x * scale + tx;
}

float mat_y( float y , float scale , float ty ) {
  return y * scale + ty;
}

void checkAndDrawSingleImage
( jint level ,
  jint id ,
  jboolean isBind ,
  float x ,
  float y ,
  float w ,
  float h ,
  jint surfaceWidth ,
  jint surfaceHeight ,
  jint blankId ) {
  if ( x + w >= 0 && x < surfaceWidth && y + h >= 0 && y < surfaceHeight ) {
    if ( isBind ) {
      Java_jp_archilogic_docnext_android_coreview_image_CoreImageRenderEngine_drawSingleImageJNI( 0 , 0 , id , x , y , w , h );
    } else if ( level == 0 ) {
      Java_jp_archilogic_docnext_android_coreview_image_CoreImageRenderEngine_drawSingleImageJNI( 0 , 0 , blankId , x , y , w , h );
    }
  }
}

void Java_jp_archilogic_docnext_android_coreview_image_CoreImageRenderEngine_drawSingleImageJNI
( JNIEnv *env,
  jobject obj,
  jint id,
  jfloat x,
  jfloat y,
  jfloat w,
  jfloat h ) {
  glBindTexture( GL_TEXTURE_2D , id );
  glDrawTexfOES( x , y , 0 , w , h );
}

void Java_jp_archilogic_docnext_android_coreview_image_CoreImageRenderEngine_drawImageJNI
( JNIEnv *env,
  jobject obj,
  jfloat scale,
  jfloat tx,
  jfloat ty,
  jfloat hPadding,
  jfloat vPadding,
  jobjectArray textures,
  jobjectArray isBind,
  jint xSign,
  jint ySign,
  jint surfaceWidth,
  jint surfaceHeight,
  jint pageWidth,
  jint pageHeight,
  jint page,
  jint pages,
  jfloatArray lastTextureHeight,
  jint blankId ) {
  // __android_log_write( ANDROID_LOG_DEBUG , "Tag" , msg );

  jsize nLevel = (*env)->GetArrayLength( env , textures );

  int level;
  int delta;
  int py;
  int px;

  jfloat* lastHeightArr = (*env)->GetFloatArrayElements( env , lastTextureHeight , 0 );

  for ( level = 0 ; level < nLevel ; level++ ) {
    if ( level > 0 && scale < pow( 2 , level - 1 ) ) {
      continue;
    }

    jobjectArray texLevelArr = (*env)->GetObjectArrayElement( env , textures , level );
    jobjectArray bindLevelArr = (*env)->GetObjectArrayElement( env , isBind , level );
    float factor = pow( 2 , level );
    float size = mat_len( TEXTURE_SIZE , scale ) / factor;


    for ( delta = -1 ; delta <= 1 ; delta++ ) {
      int p = page + delta;

      if ( p < 0 || p >= pages ) {
        continue;
      }

      jobjectArray texPageArr = (*env)->GetObjectArrayElement( env , texLevelArr , delta + 1 );
      jobjectArray bindPageArr = (*env)->GetObjectArrayElement( env , bindLevelArr , delta + 1 );
      jsize ny = (*env)->GetArrayLength( env , texPageArr );

      float y = surfaceHeight - ( mat_y( TEXTURE_SIZE / factor , scale , ty ) + vPadding + mat_len( pageHeight , scale ) * delta * ySign );
      float h = ny > 1 ? size : mat_len( lastHeightArr[ level ] , scale ) / factor;

      for ( py = 0 ; py < ny ; py++ ) {
        jintArray texYArr = (jintArray) (*env)->GetObjectArrayElement( env , texPageArr , py );
        jbooleanArray bindYArr = (jbooleanArray) (*env)->GetObjectArrayElement( env , bindPageArr , py );
        jsize nx = (*env)->GetArrayLength( env , texYArr );
        jint* tex = (*env)->GetIntArrayElements( env , texYArr , 0 );
        jboolean* bind = (*env)->GetBooleanArrayElements( env , bindYArr , 0 );

        float x = mat_x( 0 , scale , tx ) + hPadding + mat_len( pageWidth , scale ) * delta * xSign;

        for ( px = 0 ; px < nx ; px++ ) {
          checkAndDrawSingleImage( level , tex[ px ] , bind[ px ] , x , y , size , h , surfaceWidth , surfaceHeight , blankId );

          x += size;
        }

        if ( py + 1 == ny - 1 ) {
          h = mat_len( lastHeightArr[ level ] , scale ) / factor;
        }

        y -= h;

        (*env)->ReleaseBooleanArrayElements( env , bindYArr , bind , JNI_ABORT );
        (*env)->ReleaseIntArrayElements( env , texYArr , tex , JNI_ABORT );
        //(*env)->ReleaseObjectArrayElements( env , bindPageArr , bindYArr , JNI_ABORT );
        //(*env)->ReleaseObjectArrayElements( env , texPageArr , texYArr , JNI_ABORT );
      }

      //(*env)->ReleaseObjectArrayElements( env , bindLevelArr , bindPageArr , JNI_ABORT );
      //(*env)->ReleaseObjectArrayElements( env , texLevelArr , texPageArr , JNI_ABORT );
    }

    //(*env)->ReleaseObjectArrayElements( env , isBind , bindLevelArr , JNI_ABORT );
    //(*env)->ReleaseObjectArrayElements( env , textures , texLevelArr , JNI_ABORT );
  }

  (*env)->ReleaseFloatArrayElements( env , lastTextureHeight , lastHeightArr , JNI_ABORT );
}

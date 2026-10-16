-keep class org.videolan.libvlc.**{*;}

-keepclasseswithmembernames,includedescriptorclasses class *{
native <methods>;
}

-keepattributes Signature,InnerClasses,EnclosingMethod,AnnotationDefault
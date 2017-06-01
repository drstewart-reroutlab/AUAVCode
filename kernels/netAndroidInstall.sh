cp $AUAVHOME/KernelForAndroid/app/build/outputs/apk/app-debug.apk KernelForAndroid.apk
scp -i $AUAVHOME/META/dronephone_rsa -P 1234 KernelForAndroid.apk reroutlab@$1:.

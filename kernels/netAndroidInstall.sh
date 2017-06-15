cp $AUAVHOME/AUAVAndroid/app/app-release.apk AUAVAndroid.apk
scp -i $AUAVHOME/META/dronephone_rsa -P 1234 AUAVAndroid.apk reroutlab@$1:.

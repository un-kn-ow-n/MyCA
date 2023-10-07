
var rand = function(min, max){
    return Math.floor(Math.random() * (max - min)) + min;
}

var genAESKey = function(bit){
    var lib = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    var key = "";
    for(var i = 0; i < bit; i++){
        key += lib.charAt(rand(0, lib.length));
    }
    return key;
}

var  AESencrypt = function(m, keyStr){
    var key = CryptoJS.enc.Utf8.parse(keyStr);
    var srcs = CryptoJS.enc.Utf8.parse(m);
    var encrypted = CryptoJS.AES.encrypt(srcs, key,
            {
                mode: CryptoJS.mode.ECB,
                padding: CryptoJS.pad.Pkcs7
            }
        );
    return encrypted.toString();
}

var AESdecrypt = function(c, keyStr){
    var decrypt = CryptoJS.AES.decrypt(c, CryptoJS.enc.Utf8.parse(keyStr),
            {
                mode: CryptoJS.mode.ECB,
                padding: CryptoJS.pad.Pkcs7
            }
        );
    return CryptoJS.enc.Utf8.stringify(decrypt).toString();
}
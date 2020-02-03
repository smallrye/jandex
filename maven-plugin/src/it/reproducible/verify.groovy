def jandexFile = new File(basedir, 'target/classes/META-INF/jandex.idx')
def jandexFile2 = new File(basedir, 'target/classes/META-INF/jandex-2.idx')

assert jandexFile.exists() : "File does not exist: ${jandexFile}"
assert jandexFile2.exists() : "File does not exist: ${jandexFile}"

byte[] idx = jandexFile.bytes
byte[] idx2 = jandexFile2.bytes

assert idx == idx2

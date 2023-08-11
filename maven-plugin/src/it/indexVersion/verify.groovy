import org.jboss.jandex.IndexReader
import org.jboss.jandex.PackedDataInputStream

def jandexFile = new File(basedir, 'target/classes/META-INF/jandex.idx')
assert jandexFile.exists() : "File ${jandexFile} does not exist"
assert jandexFile.length() > 0 : "File ${jandexFile} is empty"

def data = new PackedDataInputStream(jandexFile.newInputStream())
assert data.readInt() == 0xBABE1F15 as int // magic
assert data.readUnsignedByte() == 6 // version

def index = new IndexReader(jandexFile.newInputStream()).read()
assert index.getKnownClasses().size() == 1 : "Index ${jandexFile} does not contain exactly 1 class"

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.controller.data;

import com.mysql.jdbc.Util;
import core.controller.utils.Utilities;
import java.io.File;
import java.nio.charset.Charset;
import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.serialization.UaDecoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaEncoder;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.GenericDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.SerializationContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;

/**
 *
 * @author Jan-Peter.Schmidt
 */
public class FileWrapperOPCUA {
    
    private File file;
    private String endung;
    
    public FileWrapperOPCUA(File file) {
        
        this.file = file;
        this.endung = Utilities.getEndung(file);
        
    }
    
    public static class Codec extends GenericDataTypeCodec<FileWrapperOPCUA> {
        @Override
        public Class<FileWrapperOPCUA> getType() {
            return FileWrapperOPCUA.class;
        }

        @Override
        public FileWrapperOPCUA decode(
            SerializationContext context,
            UaDecoder decoder) throws UaSerializationException {

            try {
                String fileString = decoder.readString("fileString");
                String endung = decoder.readString("endung");
                File file = Utilities.writeFile(fileString, endung, Charset.defaultCharset());

                System.out.println("Recreated FWO: " + endung);
                return new FileWrapperOPCUA(file);
            } catch(Exception e) {
                return new FileWrapperOPCUA(null);
            }            
        }

        @Override
        public void encode(
            SerializationContext context,
            FileWrapperOPCUA customDataType,
            UaEncoder encoder) throws UaSerializationException {

            if(customDataType.getFile()!=null) {
                System.out.println("Break up FWO: " + customDataType.endung);
                encoder.writeString("fileString", 
                        Utilities.readFile(customDataType.getFile().getAbsolutePath(), 
                                            Charset.defaultCharset()));
                encoder.writeString("endung", customDataType.getEndung());
            } else {
                
                System.out.println("No breakup: file is null");
                
            }                        
        }
    }  

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getEndung() {
        return endung;
    }

    public void setEndung(String endung) {
        this.endung = endung;
    }
    
    
    
    
    
}

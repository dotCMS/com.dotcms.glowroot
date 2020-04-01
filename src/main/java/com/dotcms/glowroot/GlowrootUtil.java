package com.dotcms.glowroot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;


public class GlowrootUtil {

    
    final File GlowrootPathIn = new File("libs/glowroot-agent-0.13.6.jar");
    final File GlowrootPathOut =  new File(Config.CONTEXT.getRealPath("/WEB-INF/profiler/profiler.jar"));
    
    void moveJarToFS() throws FileNotFoundException, IOException {
        
        if(!GlowrootPathOut.exists()) {
            GlowrootPathOut.getParentFile().mkdirs();
            String glowPath = GlowrootPathIn.getPath();
            try ( InputStream in = new java.io.BufferedInputStream(this.getClass().getClassLoader().getResourceAsStream(glowPath))) {
                IOUtils.copy(in, new FileOutputStream(GlowrootPathOut));
            }   
            
        }

        
        
        Logger.info(this.getClass(), "glowroot.jar lives at in: " + GlowrootPathOut.getAbsolutePath());
        Logger.info(this.getClass(), "add this to your startup .sh script");
        Logger.info(this.getClass(), "-javaagent:" + GlowrootPathOut.getAbsolutePath());
        
        
        
        
    }
    

    
    
    
    
}

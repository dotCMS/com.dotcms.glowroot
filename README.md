# Glowroot Plugin

This plugin give access to the glowroot console running locally on port 4000 under the path `/glowroot`.  It proxies any incoming requests from a dotCMS instance to the local glowroot console, e.g. `/glowroot` will be proxied to `http://localhost:4000` 

The ability to proxy these requests allow us to use dotCMS security and SSL transports when accessing glowroot.  The plugin requires you to be logged in as a `CMS_Administrator` to access glowroot. 


### To install
Upload the jar files found under the `/build/libs` folder to your dotCMS instance.

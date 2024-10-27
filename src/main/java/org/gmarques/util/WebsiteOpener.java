package org.gmarques.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebsiteOpener {
    public static void openWebsite(String url) throws URISyntaxException, IOException {
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        Desktop.getDesktop().browse(new URI(url));
    }
}

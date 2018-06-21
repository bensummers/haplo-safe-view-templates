/* Haplo Safe View Templates                          http://haplo.org
 * (c) Haplo Services Ltd 2015 - 2016    http://www.haplo-services.com
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.         */

package org.haplo.template.html;

public class RenderException extends Exception {
    private Driver driver;

    public RenderException(Driver driver, String message) {
        super(message);
        this.driver = driver;
    }

    public String getMessage() {
        String templateName = "(no template)";
        if(this.driver != null) {
            Template template = driver.getLastTemplate();
            if(template != null) {
                templateName = template.getName();
            }
        }
        return "When rendering template '"+templateName+"': "+super.getMessage();
    }
}

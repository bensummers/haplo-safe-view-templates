/* Haplo Safe View Templates                          http://haplo.org
 * (c) Haplo Services Ltd 2015 - 2016    http://www.haplo-services.com
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.         */

package org.haplo.template.html;

// concat() is useful for overriding the automatic addition of spaces in attributes
final class NodeFunctionConcat extends NodeFunction {
    NodeFunctionConcat() {
    }

    public String getFunctionName() {
        return "concat";
    }

    protected Object value(Driver driver, Object view) throws RenderException {
        Node arguments = getArgumentsHead();
        if(arguments == null) { return null; }
        StringBuilder builder = new StringBuilder();
        arguments.renderWithNextNodes(builder, driver, view, Context.UNSAFE);
        return builder.toString();
    }

    public void render(StringBuilder builder, Driver driver, Object view, Context context) throws RenderException {
        Node arguments = getArgumentsHead();
        if(arguments != null) {
            arguments.renderWithNextNodes(builder, driver, view, context);
        }
    }

    public String getDumpName() {
        return "CONCAT";
    }
}

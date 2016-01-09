/* Haplo Safe View Templates                          http://haplo.org
 * (c) Haplo Services Ltd 2015 - 2016    http://www.haplo-services.com
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.         */

package org.haplo.template.driver.rhinojs;

import org.haplo.template.html.FunctionBinding;
import org.haplo.template.html.Node;
import org.haplo.template.html.Escape;
import org.haplo.template.html.DeferredRender;
import org.haplo.template.html.Context;
import org.haplo.template.html.RenderException;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

public class JSFunctionThis extends ScriptableObject {
    private StringBuilder builder;
    private FunctionBinding binding;

    public String getClassName() {
        return "$HaploTemplateFnThis";
    }

    protected void setForTemplateFnCall(StringBuilder builder, FunctionBinding binding) {
        if(this.builder != null) { throw new RuntimeException("logic error"); }
        this.builder = builder;
        this.binding = binding;
    }

    protected void resetAfterTemplateFnCall() {
        if(this.builder == null) { throw new RuntimeException("logic error"); }
        this.builder = null;
        this.binding = null;
    }

    // ----------------------------------------------------------------------

    public String jsGet_functionName() {
        return this.binding.getFunctionName();
    }

    public JSFunctionThis jsFunction_assertContext(String context) throws RenderException {
        try {
            Context expectedContext = Context.valueOf(context);
            if(this.binding.getContext() != expectedContext) {
                error("must be used in "+context+" context, attempt to use in "+
                    this.binding.getContext().name()+" context");
            }
        } catch(IllegalArgumentException e) {
            error("Unknown context name: '"+context+"'");
        }
        return this;
    }

    public JSFunctionThis jsFunction_write(String string) {
        Escape.escape(string, this.builder, this.binding.getContext());
        return this;
    }

    public JSFunctionThis jsFunction_unsafeWriteHTML(String html) {
        this.builder.append(html);
        return this;
    }

    public boolean jsFunction_hasBlock(Object blockName) throws RenderException {
        return this.binding.hasBlock(checkedBlockName(blockName));
    }

    public JSFunctionThis jsFunction_writeBlock(Object blockName) throws RenderException {
        // Use the view and context from the binding because it's being
        // rendered at the bound point in the template.
        this.binding.renderBlock(checkedBlockName(blockName), this.builder,
            this.binding.getView(), this.binding.getContext());
        return this;
    }

    public JSFunctionThis jsFunction_render(Object thing) throws RenderException {
        if(this.binding.getContext() != Context.TEXT) {
            error("can only use render() in text context");
        }
        if(thing instanceof DeferredRender) {
            ((DeferredRender)thing).renderDeferred(this.builder, this.binding.getContext());
        } else if(thing instanceof HaploTemplate) {
            // Caller can use deferredRender() on the Template object to use a different view.
            RhinoJavaScriptDriver nestedDriver = new RhinoJavaScriptDriver(this.binding.getView());
            nestedDriver.setFunctionRenderer(new JSFunctionRenderer((HaploTemplate)thing));
            ((HaploTemplate)thing).getTemplate().renderAsIncludedTemplate(
                    this.builder,
                    nestedDriver,
                    this.binding.getView(),
                    this.binding.getContext()
                );
        } else {
            error("bad object passed to render()");
        }
        return this;
    }

    // ----------------------------------------------------------------------

    private String checkedBlockName(Object blockName) throws RenderException {
        if((blockName == null) || (blockName instanceof Undefined)) {
            return Node.BLOCK_ANONYMOUS;
        } else if(blockName instanceof CharSequence) {
            return blockName.toString();
        }
        throw error("bad block name");
    }

    private RenderException error(String message) throws RenderException {
        throw new RenderException(this.binding.getDriver(),
            "In "+this.binding.getFunctionName()+"(), "+message);
    }

}

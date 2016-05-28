/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.deltahex.netbeans;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import org.exbin.deltahex.Hexadecimal;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 * Saving capability for hexadecimal editor.
 *
 * @version 0.1.0 2016/05/28
 * @author ExBin Project (http://exbin.org)
 */
class Savable extends AbstractSavable {

    private DataObject dataObject;
    private final HexEditorTopComponent component;
    private final Hexadecimal hexEditor;

    public Savable(HexEditorTopComponent component, Hexadecimal hexEditor) {
        this.component = component;
        this.hexEditor = hexEditor;
    }

    public void activate() {
        register();
    }

    public void deactivate() {
        unregister();
    }

    public void setDataObject(DataObject dataObject) {
        this.dataObject = dataObject;
    }

    @Override
    protected String findDisplayName() {
        return dataObject == null ? "<unknown file>" : dataObject.getPrimaryFile().getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Savable other = (Savable) obj;
        return Objects.equals(this.component, other.component);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    protected void handleSave() throws IOException {
        if (dataObject == null) {
            return;
        }

        OutputStream stream = dataObject.getPrimaryFile().getOutputStream();
        try {
            hexEditor.getData().saveToStream(stream);
            stream.flush();
            component.setModified(false);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (null != stream) {
                stream.close();
            }
        }
    }
}
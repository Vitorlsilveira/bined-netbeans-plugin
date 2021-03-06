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
package org.exbin.bined.netbeans;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import org.exbin.bined.EditationMode;
import org.exbin.bined.delta.DeltaDocument;
import org.exbin.bined.delta.FileDataSource;
import org.exbin.bined.delta.SegmentsRepository;
import org.exbin.bined.swing.basic.CodeArea;
import org.exbin.utils.binary_data.BinaryData;
import org.exbin.utils.binary_data.EditableBinaryData;
import org.exbin.utils.binary_data.PagedData;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;

/**
 * Hexadecimal editor node.
 *
 * @version 0.2.0 2018/09/10
 * @author ExBin Project (http://exbin.org)
 */
public class HexEditorNode extends AbstractNode {

    private final HexEditorTopComponent hexEditorTopComponent;

    public HexEditorNode(HexEditorTopComponent hexEditorTopComponent) {
        super(Children.LEAF);
        this.hexEditorTopComponent = hexEditorTopComponent;
    }

    public void openFile(DataObject dataObject) {
        CodeArea codeArea = hexEditorTopComponent.getCodeArea();
        boolean editable = dataObject.getPrimaryFile().canWrite();
        SegmentsRepository segmentsRepository = HexEditorTopComponent.getSegmentsRepository();
        URI fileUri = dataObject.getPrimaryFile().toURI();
        if (fileUri == null) {
            InputStream stream = null;
            try {
                stream = dataObject.getPrimaryFile().getInputStream();
                if (stream != null) {
                    ((EditableBinaryData) codeArea.getContentData()).loadFromStream(stream);
                    codeArea.setEditationMode(editable ? EditationMode.OVERWRITE : EditationMode.READ_ONLY);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        } else {
            try {
                BinaryData oldData = codeArea.getContentData();
                codeArea.setEditationMode(editable ? EditationMode.OVERWRITE : EditationMode.READ_ONLY);
                File file = new File(fileUri);
                if (hexEditorTopComponent.isDeltaMemoryMode()) {
                    FileDataSource fileSource = segmentsRepository.openFileSource(file, editable ? FileDataSource.EditationMode.READ_WRITE : FileDataSource.EditationMode.READ_ONLY);
                    DeltaDocument document = segmentsRepository.createDocument(fileSource);
                    codeArea.setContentData(document);
                    oldData.dispose();
                } else {
                    try (FileInputStream fileStream = new FileInputStream(file)) {
                        BinaryData data = codeArea.getContentData();
                        if (!(data instanceof PagedData)) {
                            data = new PagedData();
                            oldData.dispose();
                        }
                        ((EditableBinaryData) data).loadFromStream(fileStream);
                        codeArea.setContentData(data);
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void saveFile(DataObject dataObject) {
        CodeArea codeArea = hexEditorTopComponent.getCodeArea();
        SegmentsRepository segmentsRepository = HexEditorTopComponent.getSegmentsRepository();
        BinaryData data = codeArea.getContentData();
        if (data instanceof DeltaDocument) {
            try {
                segmentsRepository.saveDocument((DeltaDocument) data);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            OutputStream stream;
            try {
                stream = dataObject.getPrimaryFile().getOutputStream();
                try {
                    codeArea.getContentData().saveToStream(stream);
                    stream.flush();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}

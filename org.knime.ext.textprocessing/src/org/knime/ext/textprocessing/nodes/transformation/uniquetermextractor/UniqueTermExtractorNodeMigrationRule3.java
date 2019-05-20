/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 */

package org.knime.ext.textprocessing.nodes.transformation.uniquetermextractor;

import java.util.Set;

import org.knime.base.node.preproc.rename.RenameNodeFactory;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.workflow.migration.MigrationException;
import org.knime.node.workflow.migration.MigrationNodeMatchResult;
import org.knime.node.workflow.migration.NodeMigrationAction;
import org.knime.node.workflow.migration.NodeMigrationRule;
import org.knime.node.workflow.migration.model.MigrationNode;
import org.knime.node.workflow.migration.model.MigrationNodeConnection;

/**
 * @author Marc Bux, KNIME GmbH, Berlin, Germany
 */
public class UniqueTermExtractorNodeMigrationRule3 extends NodeMigrationRule {

    @Override
    protected Class<? extends NodeFactory<?>> getReplacementNodeFactoryClass(final MigrationNode migrationNode,
        final MigrationNodeMatchResult matchResult) {
        return RenameNodeFactory.class;
    }

    MigrationNode predecessor;

    @Override
    protected MigrationNodeMatchResult match(final MigrationNode migrationNode) {

        boolean isGroupBy = "org.knime.base.node.preproc.groupby.GroupByNodeFactory"
            .equals(migrationNode.getOriginalNodeFactoryClassName());

        boolean singleTwoLevelPredecessorExistsAndIsBOWCreator = false;
        boolean singlePredecessorExistsAndIsTerm2String = false;

        if (isGroupBy) {
            Set<MigrationNodeConnection> inConnections =
                migrationNode.getOriginalInputPorts().get(1).getConnections();
            if (inConnections.size() == 1) {
                predecessor = inConnections.iterator().next().getSourcePort().getMigrationNode();
                String className = predecessor.getOriginalNodeFactoryClassName();
                singlePredecessorExistsAndIsTerm2String = "org.knime.ext.textprocessing.nodes.transformation.termtostring.TermToStringNodeFactory"
                    .equals(className);
                if (singlePredecessorExistsAndIsTerm2String) {
                    inConnections =
                            predecessor.getOriginalInputPorts().get(1).getConnections();
                    if (inConnections.size() == 1) {
                        predecessor = inConnections.iterator().next().getSourcePort().getMigrationNode();
                        className = predecessor.getOriginalNodeFactoryClassName();
                        singleTwoLevelPredecessorExistsAndIsBOWCreator = "org.knime.ext.textprocessing.nodes.transformation.bow.BagOfWordsNodeFactory2"
                                .equals(className);
                    }
                }
            }
        }

        return MigrationNodeMatchResult.of(migrationNode,
            isGroupBy && singleTwoLevelPredecessorExistsAndIsBOWCreator && singlePredecessorExistsAndIsTerm2String
                ? NodeMigrationAction.REPLACE : null);
    }

    @Override
    protected void migrate(final MigrationNode migrationNode, final MigrationNodeMatchResult matchResult)
        throws MigrationException {
        NodeSettingsWO settings = getNewNodeModelSettings(migrationNode);
        NodeSettings all = new NodeSettings("all_columns");
        NodeSettings sub1 = new NodeSettings("0");
        sub1.addString("old_column_name", "Term");
        sub1.addString("new_column_name", "Term as String");
        sub1.addInt("new_column_type", 0);
        all.addNodeSettings(sub1);
        NodeSettings sub2 = new NodeSettings("1");
        sub2.addString("old_column_name", "DF");
        sub2.addString("new_column_name", "Document");
        sub2.addInt("new_column_type", 0);
        all.addNodeSettings(sub2);
        settings.addNodeSettings(all);
        associateOriginalPortWithNew(migrationNode.getOriginalOutputPorts().get(1), migrationNode.getNewOutputPorts().get(1));
    }

    @Override
    public String getMigrationType() {
        return "Specialized pattern detected";
    }

}

package org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelUtil;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import lombok.val;

public class ObjectAdapterActionColumn extends ColumnAbstract<ManagedObject> {

    private static final long serialVersionUID = 1L;

    private final EntityCollectionModel.Variant collectionVariant;
    private final String parentTypeName;

    public ObjectAdapterActionColumn(
            IsisAppCommonContext commonContext, 
            EntityCollectionModel.Variant collectionVariant,
            IModel<String> columnNameModel,
            String parentTypeName) {
        
        super(commonContext, columnNameModel, null);
        this.collectionVariant = collectionVariant;
        this.parentTypeName = parentTypeName;
    }

    @Override
    public Component getHeader(final String componentId) {
        final Label label = new Label(componentId, getDisplayModel());
        return label;
    }

    @Override
    public String getCssClass() {
        final String cssClass = super.getCssClass();
        return (!_Strings.isNullOrEmpty(cssClass) ? (cssClass + " ") : "") +
                CssClassAppender.asCssStyle("isis-" + parentTypeName.replace(".","-") + "-actionsColumn");
    }

    @Override
    public void populateItem(
            final Item<ICellPopulator<ManagedObject>> cellItem, 
            final String componentId, 
            final IModel<ManagedObject> rowModel) {
        
        final Component component = createComponent(componentId, rowModel);
        cellItem.add(component);
        cellItem.add(new CssClassAppender("action-column"));
    }

    private Component createComponent(final String id, final IModel<ManagedObject> rowModel) {
        val adapter = rowModel.getObject();
        final EntityModel entityModel = EntityModel.ofAdapter(super.getCommonContext(), adapter);
        final List<ObjectAction> topLevelActions = ObjectAction.Util.findTopLevel(adapter);
        final List<LinkAndLabel> entityActionLinks = LinkAndLabelUtil.asActionLinksForAdditionalLinksPanel(entityModel, topLevelActions, null);
        if(topLevelActions.size() > 1) {
        	return AdditionalLinksPanel.Style.DROPDOWN.newPanel(id, entityActionLinks);
        } else {
        	return AdditionalLinksPanel.Style.INLINE_LIST.newPanel(id, entityActionLinks);
        }
    }
}

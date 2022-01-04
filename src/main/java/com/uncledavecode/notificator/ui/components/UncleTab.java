package com.uncledavecode.notificator.ui.components;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

public class UncleTab extends VerticalLayout {

    private Map<Tab, Component> tabsToPages = new HashMap<>();
    private Tabs tabs = new Tabs();
    private Div contents = new Div();
    private Component selectedPage = null;

    public UncleTab() {
        this.contents.setSizeFull();
        this.add(tabs, contents);
        this.setSizeFull();
        tabs.addSelectedChangeListener(event -> {
            if (selectedPage != null) {
                selectedPage.setVisible(false);

                selectedPage = tabsToPages.get(tabs.getSelectedTab());

                selectedPage.setVisible(true);
            }
        });
    }

    public Tab addTab(String caption, Component content) {
        Tab result = new Tab(caption);

        tabsToPages.put(result, content);

        contents.add(content);

        content.setVisible(false);

        this.tabs.add(result);

        if (selectedPage == null) {
            content.setVisible(true);
            selectedPage = content;
        }

        return result;
    }
}

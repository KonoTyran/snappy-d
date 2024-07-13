package dev.koifysh.snappy;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@PluginDescriptor(
		name = "Snappy D",
		description = "no more saying \"snapdragon\" that is not as cool."
)
public class D extends Plugin
{
	@Inject
	private Client client;

	private static final ImmutableMap<String, String> ItemNameRemap = ImmutableMap.<String, String>builder()
		.put("snapdragon", "Snappy D")
		.put("Grimy snapdragon", "Grimy Snappy D")
		.put("Snapdragon seed", "Snappy D seed")
		.build();

	private static final Set<MenuAction> ITEM_MENU_ACTIONS = ImmutableSet.of(
		MenuAction.GROUND_ITEM_FIRST_OPTION, MenuAction.GROUND_ITEM_SECOND_OPTION,
		MenuAction.GROUND_ITEM_THIRD_OPTION, MenuAction.GROUND_ITEM_FOURTH_OPTION,
		MenuAction.GROUND_ITEM_FIFTH_OPTION, MenuAction.EXAMINE_ITEM_GROUND,
		// Inventory + Using Item on Players/NPCs/Objects
		MenuAction.CC_OP, MenuAction.CC_OP_LOW_PRIORITY, MenuAction.WIDGET_TARGET,
		MenuAction.WIDGET_TARGET_ON_PLAYER, MenuAction.WIDGET_TARGET_ON_NPC,
		MenuAction.WIDGET_TARGET_ON_GAME_OBJECT, MenuAction.WIDGET_TARGET_ON_GROUND_ITEM,
		MenuAction.WIDGET_TARGET_ON_WIDGET);

	@Override
	protected void startUp() throws Exception
	{
		log.info("Snappy D started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Snappy D stopped!");
	}


    private void remapWidget(Widget widget) {
        final int groupId = WidgetInfo.TO_GROUP(widget.getId());
        final int CHAT_MESSAGE = 162, PRIVATE_MESSAGE = 163, FRIENDS_LIST = 429;

        if (groupId == CHAT_MESSAGE || groupId == PRIVATE_MESSAGE || groupId == FRIENDS_LIST)
            return;

        Widget[] children = widget.getDynamicChildren();
        if (children == null)
            return;

        Widget[] childComponents = widget.getDynamicChildren();
        if (childComponents != null)
            mapWidgetText(childComponents);

        childComponents = widget.getStaticChildren();
        if (childComponents != null)
            mapWidgetText(childComponents);

        childComponents = widget.getNestedChildren();
        if (childComponents != null)
            mapWidgetText(childComponents);
    }

	private void mapWidgetText(Widget[] childComponents) {
        for (Widget component : childComponents) {
            remapWidget(component);

            String text = component.getText();
            if (text.isEmpty())
                continue;

			RemapWidgetText(component, text, ItemNameRemap);
        }
    }

	private void RemapWidgetText(Widget component, String text, ImmutableMap<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (text.equalsIgnoreCase(entry.getKey())) {
                component.setText(text.replace(entry.getKey(), entry.getValue()));
                return;
            }
        }
    }

	@Subscribe
    protected void onMenuEntryAdded(MenuEntryAdded event) {
        MenuEntry entry = event.getMenuEntry();

		if (ITEM_MENU_ACTIONS.contains(entry.getType())) {
			RemapMenuEntryText(entry, ItemNameRemap);
        }
    }

	private void RemapMenuEntryText(MenuEntry menuEntry, Map<String, String> map) {
        String target = menuEntry.getTarget();
        NPC npc = menuEntry.getNpc();
        String cleanTarget = null;
        if (npc != null)
            cleanTarget = Text.removeTags(npc.getName());
        else
            cleanTarget = Text.removeTags(target);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (cleanTarget.equals(entry.getKey())) {
                menuEntry.setTarget(target.replace(entry.getKey(), entry.getValue()));
            }
        }
    }

	private void RemapMenuEntryText(MenuEntry menuEntry, ImmutableMap<String, String> map) {
        RemapMenuEntryText(menuEntry, (Map<String, String>) map);
    }

    @Subscribe
    private void onBeforeRender(BeforeRender event) {
        if (client.getGameState() != GameState.LOGGED_IN)
            return;

        for (Widget widgetRoot : client.getWidgetRoots()) {
            remapWidget(widgetRoot);
        }
    }

}

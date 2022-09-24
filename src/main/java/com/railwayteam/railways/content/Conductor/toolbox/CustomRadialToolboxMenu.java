package com.railwayteam.railways.content.Conductor.toolbox;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.content.Conductor.ConductorEntity;
import com.railwayteam.railways.mixin.AccessorToolboxInventory;
import com.railwayteam.railways.mixin.AccessorToolboxTileEntity;
import com.railwayteam.railways.mixin.client.AccessorToolboxHandlerClient;
import com.railwayteam.railways.registry.CRPackets;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.curiosities.toolbox.ToolboxDisposeAllPacket;
import com.simibubi.create.content.curiosities.toolbox.ToolboxEquipPacket;
import com.simibubi.create.content.curiosities.toolbox.ToolboxInventory;
import com.simibubi.create.content.curiosities.toolbox.ToolboxTileEntity;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.simibubi.create.content.curiosities.toolbox.RadialToolboxMenu.State;
import static com.simibubi.create.content.curiosities.toolbox.ToolboxInventory.STACKS_PER_COMPARTMENT;

public class CustomRadialToolboxMenu extends AbstractSimiScreen {

	private State state;
	private int ticksOpen;
	private int hoveredSlot;
	private boolean scrollMode;
	private int scrollSlot = 0;
	private List<Object> toolboxObjects; //ToolboxTileEntity | MountedToolboxHolder=
	private Object selectedObject; //ToolboxTileEntity | MountedToolboxHolder

	private static final int DEPOSIT = -7;
	private static final int UNEQUIP = -5;

	public CustomRadialToolboxMenu(List<ToolboxTileEntity> toolboxes, State state, @Nullable ToolboxTileEntity selectedBox) {
		this(toolboxes, List.of(), state, selectedBox);
	}

	public CustomRadialToolboxMenu(List<ToolboxTileEntity> toolboxes, List<ConductorEntity> conductors, State state, @Nullable ToolboxTileEntity selectedBox) {
		this.toolboxObjects = new ArrayList<>();
		conductors.stream()
				.filter(ConductorEntity::isCarryingToolbox)
				.map(ConductorEntity::getToolboxHolder)
				.forEach(toolboxObjects::add);
		toolboxObjects.addAll(toolboxes);
		/*toolboxObjects.forEach((obj) -> {
			if (obj instanceof ToolboxTileEntity te) {
				Railways.LOGGER.info("TE: "+te.getDisplayName()+"@"+te.getBlockPos());
			} else if (obj instanceof MountedToolboxHolder mt) {
				Railways.LOGGER.info("MT: "+mt.getDisplayName()+"@"+mt.parent.position());
			}
		});*/
		this.state = state;
		hoveredSlot = -1;

		if (selectedBox != null)
			this.selectedObject = selectedBox;
	}

	public CustomRadialToolboxMenu(List<ToolboxTileEntity> toolboxes, List<ConductorEntity> conductors, State state, @Nullable ConductorEntity selectedConductor) {
		this.toolboxObjects = new ArrayList<>();
		conductors.stream()
				.filter(ConductorEntity::isCarryingToolbox)
				.map(ConductorEntity::getToolboxHolder)
				.forEach(toolboxObjects::add);
		toolboxObjects.addAll(toolboxes);
		/*toolboxObjects.forEach((obj) -> {
			if (obj instanceof ToolboxTileEntity te) {
				Railways.LOGGER.info("TE: "+te.getDisplayName()+"@"+te.getBlockPos());
			} else if (obj instanceof MountedToolboxHolder mt) {
				Railways.LOGGER.info("MT: "+mt.getDisplayName()+"@"+mt.parent.position());
			}
		});*/
		this.state = state;
		hoveredSlot = -1;

		if (selectedConductor != null && selectedConductor.isCarryingToolbox())
			this.selectedObject = selectedConductor.getToolboxHolder();
	}

	public void prevSlot(int slot) {
		scrollSlot = slot;
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		float fade = Mth.clamp((ticksOpen + AnimationTickHolder.getPartialTicks()) / 10f, 1 / 512f, 1);

		hoveredSlot = -1;
		Window window = getMinecraft().getWindow();
		float hoveredX = mouseX - window.getGuiScaledWidth() / 2;
		float hoveredY = mouseY - window.getGuiScaledHeight() / 2;

		float distance = hoveredX * hoveredX + hoveredY * hoveredY;
		if (distance > 25 && distance < 10000)
			hoveredSlot =
				(Mth.floor((AngleHelper.deg(Mth.atan2(hoveredY, hoveredX)) + 360 + 180 - 22.5f)) % 360)
					/ 45;
		boolean renderCenterSlot = state == State.SELECT_ITEM_UNEQUIP;
		if (scrollMode && distance > 150)
			scrollMode = false;
		if (renderCenterSlot && distance <= 150)
			hoveredSlot = UNEQUIP;

		ms.pushPose();
		ms.translate(width / 2, height / 2, 0);
		Component tip = null;

		if (state == State.DETACH) {

			tip = Lang.translateDirect("toolbox.outOfRange");
			if (hoveredX > -20 && hoveredX < 20 && hoveredY > -80 && hoveredY < -20)
				hoveredSlot = UNEQUIP;

			ms.pushPose();
			AllGuiTextures.TOOLBELT_INACTIVE_SLOT.render(ms, -12, -12, this);
			GuiGameElement.of(AllBlocks.TOOLBOXES.get(DyeColor.BROWN)
				.asStack())
				.at(-9, -9)
				.render(ms);

			ms.translate(0, -40 + (10 * (1 - fade) * (1 - fade)), 0);
			AllGuiTextures.TOOLBELT_SLOT.render(ms, -12, -12, this);
			ms.translate(-0.5, 0.5, 0);
			AllIcons.I_DISABLE.render(ms, -9, -9, this);
			ms.translate(0.5, -0.5, 0);
			if (!scrollMode && hoveredSlot == UNEQUIP) {
				AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(ms, -13, -13, this);
				tip = Lang.translateDirect("toolbox.detach")
					.withStyle(ChatFormatting.GOLD);
			}
			ms.popPose();

		} else {

			if (hoveredX > 60 && hoveredX < 100 && hoveredY > -20 && hoveredY < 20)
				hoveredSlot = DEPOSIT;

			ms.pushPose();
			ms.translate(80 + (-5 * (1 - fade) * (1 - fade)), 0, 0);
			AllGuiTextures.TOOLBELT_SLOT.render(ms, -12, -12, this);
			ms.translate(-0.5, 0.5, 0);
			AllIcons.I_TOOLBOX.render(ms, -9, -9, this);
			ms.translate(0.5, -0.5, 0);
			if (!scrollMode && hoveredSlot == DEPOSIT) {
				AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(ms, -13, -13, this);
				tip = Lang.translateDirect(state == State.SELECT_BOX ? "toolbox.depositAll" : "toolbox.depositBox")
					.withStyle(ChatFormatting.GOLD);
			}
			ms.popPose();

			for (int slot = 0; slot < 8; slot++) {
				ms.pushPose();
				TransformStack.cast(ms)
					.rotateZ(slot * 45 - 45)
					.translate(0, -40 + (10 * (1 - fade) * (1 - fade)), 0)
					.rotateZ(-slot * 45 + 45);
				ms.translate(-12, -12, 0);

				if (state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
					ItemStack stackInSlot = ItemStack.EMPTY;
					Object inv = null;
					if (selectedObject instanceof ToolboxTileEntity selectedBox) {
						ToolboxInventory tinv = ((AccessorToolboxTileEntity) selectedBox).getInventory();
						stackInSlot = ((AccessorToolboxInventory) tinv).getFilters().get(slot);
						inv = tinv;
					} else if (selectedObject instanceof MountedToolboxHolder selectedHolder) {
						MountedToolboxInventory mtinv = selectedHolder.inventory;
						stackInSlot = mtinv.filters.get(slot);
						inv = mtinv;
					}

					if (!stackInSlot.isEmpty()) {
						boolean empty = true;
						if (inv instanceof ToolboxInventory tinv) {
							empty = tinv.getStackInSlot(slot * STACKS_PER_COMPARTMENT)
									.isEmpty();
						} else if (inv instanceof MountedToolboxInventory mtinv) {
							empty = mtinv.getStackInSlot(slot * STACKS_PER_COMPARTMENT)
									.isEmpty();
						}

						(empty ? AllGuiTextures.TOOLBELT_INACTIVE_SLOT : AllGuiTextures.TOOLBELT_SLOT)
							.render(ms, 0, 0, this);
						GuiGameElement.of(stackInSlot)
							.at(3, 3)
							.render(ms);

						if (slot == (scrollMode ? scrollSlot : hoveredSlot) && !empty) {
							AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(ms, -1, -1, this);
							tip = stackInSlot.getHoverName();
						}
					} else
						AllGuiTextures.TOOLBELT_EMPTY_SLOT.render(ms, 0, 0, this);

				} else if (state == State.SELECT_BOX) {

					if (slot < toolboxObjects.size()) {
						AllGuiTextures.TOOLBELT_SLOT.render(ms, 0, 0, this);
						Object obj = toolboxObjects.get(slot);
						if (obj instanceof ToolboxTileEntity toolboxTileEntity) {
							GuiGameElement.of(AllBlocks.TOOLBOXES.get(toolboxTileEntity.getColor())
											.asStack())
									.at(3, 3)
									.render(ms);

							if (slot == (scrollMode ? scrollSlot : hoveredSlot)) {
								AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(ms, -1, -1, this);
								tip = toolboxTileEntity.getDisplayName();
							}
						} else if (obj instanceof MountedToolboxHolder mountedToolboxHolder) {
							GuiGameElement.of(AllBlocks.TOOLBOXES.get(mountedToolboxHolder.getColor())
											.asStack())
									.at(3, 3)
									.render(ms);

							if (slot == (scrollMode ? scrollSlot : hoveredSlot)) {
								AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(ms, -1, -1, this);
								tip = mountedToolboxHolder.getDisplayName();
							}
						}
					} else
						AllGuiTextures.TOOLBELT_EMPTY_SLOT.render(ms, 0, 0, this);

				}

				ms.popPose();
			}

			if (renderCenterSlot) {
				ms.pushPose();
				AllGuiTextures.TOOLBELT_SLOT.render(ms, -12, -12, this);
				(scrollMode ? AllIcons.I_REFRESH : AllIcons.I_FLIP).render(ms, -9, -9, this);
				if (!scrollMode && UNEQUIP == hoveredSlot) {
					AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(ms, -13, -13, this);
					tip = Lang.translateDirect("toolbox.unequip", minecraft.player.getMainHandItem()
						.getHoverName())
						.withStyle(ChatFormatting.GOLD);
				}
				ms.popPose();
			}
		}
		ms.popPose();

		if (tip != null) {
			int i1 = (int) (fade * 255.0F);
			if (i1 > 255)
				i1 = 255;

			if (i1 > 8) {
				ms.pushPose();
				ms.translate((float) (width / 2), (float) (height - 68), 0.0F);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				int k1 = 16777215;
				int k = i1 << 24 & -16777216;
				int l = font.width(tip);
				font.draw(ms, tip, (float) (-l / 2), -4.0F, k1 | k);
				RenderSystem.disableBlend();
				ms.popPose();
			}
		}

	}

	@Override
	public void renderBackground(PoseStack p_238651_1_, int p_238651_2_) {
		int a = ((int) (0x50 * Math.min(1, (ticksOpen + AnimationTickHolder.getPartialTicks()) / 20f))) << 24;
		fillGradient(p_238651_1_, 0, 0, this.width, this.height, 0x101010 | a, 0x101010 | a);
	}

	@Override
	public void tick() {
		ticksOpen++;
		super.tick();
	}

	@Override
	public void removed() {
		super.removed();

		int selected = (scrollMode ? scrollSlot : hoveredSlot);

		if (selected == DEPOSIT) {
			if (state == State.DETACH)
				return;
			else if (state == State.SELECT_BOX)
				toolboxObjects.forEach(obj -> {
					if (obj instanceof ToolboxTileEntity te)
						AllPackets.channel.sendToServer(new ToolboxDisposeAllPacket(te.getBlockPos()));
					else if (obj instanceof MountedToolboxHolder mt)
						CRPackets.channel.sendToServer(new MountedToolboxDisposeAllPacket(mt.parent));
				});
			else {
				if (selectedObject instanceof ToolboxTileEntity te)
					AllPackets.channel.sendToServer(new ToolboxDisposeAllPacket(te.getBlockPos()));
				else if (selectedObject instanceof MountedToolboxHolder mt)
					CRPackets.channel.sendToServer(new MountedToolboxDisposeAllPacket(mt.parent));
			}
			return;
		}

		if (state == State.SELECT_BOX)
			return;

		if (state == State.DETACH) {
			if (selected == UNEQUIP)
				AllPackets.channel.sendToServer(
					new ToolboxEquipPacket(null, selected, minecraft.player.getInventory().selected));
			return;
		}

		if (selected == UNEQUIP) {
			if (selectedObject instanceof ToolboxTileEntity te)
				AllPackets.channel.sendToServer(new ToolboxEquipPacket(te.getBlockPos(), selected,
						minecraft.player.getInventory().selected));
			else if (selectedObject instanceof MountedToolboxHolder mt)
				CRPackets.channel.sendToServer(new MountedToolboxEquipPacket(mt.parent, selected,
						minecraft.player.getInventory().selected));
		}

		if (selected < 0)
			return;
		ItemStack stackInSlot = ItemStack.EMPTY;
		ItemStackHandler inv = null;
		if (selectedObject instanceof ToolboxTileEntity te) {
			ToolboxInventory tinv = ((AccessorToolboxTileEntity) te).getInventory();
			stackInSlot = ((AccessorToolboxInventory) tinv).getFilters().get(selected);
			inv = tinv;
		} else if (selectedObject instanceof MountedToolboxHolder mt) {
			MountedToolboxInventory mtinv = mt.inventory;
			stackInSlot = mtinv.filters.get(selected);
			inv = mtinv;
		}
		if (stackInSlot.isEmpty())
			return;
		if (inv.getStackInSlot(selected * STACKS_PER_COMPARTMENT)
			.isEmpty())
			return;

		if (selectedObject instanceof ToolboxTileEntity te)
			AllPackets.channel.sendToServer(new ToolboxEquipPacket(te.getBlockPos(), selected,
				minecraft.player.getInventory().selected));
		else if (selectedObject instanceof MountedToolboxHolder mt)
			CRPackets.channel.sendToServer(new MountedToolboxEquipPacket(mt.parent, selected,
					minecraft.player.getInventory().selected));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		Window window = getMinecraft().getWindow();
		double hoveredX = mouseX - window.getGuiScaledWidth() / 2;
		double hoveredY = mouseY - window.getGuiScaledHeight() / 2;
		double distance = hoveredX * hoveredX + hoveredY * hoveredY;
		if (distance <= 150) {
			scrollMode = true;
			scrollSlot = (((int) (scrollSlot - delta)) + 8) % 8;
			for (int i = 0; i < 10; i++) {

				if (state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
					ItemStack stackInSlot = ItemStack.EMPTY;
					ItemStackHandler inv = null;
					if (selectedObject instanceof ToolboxTileEntity te) {
						ToolboxInventory tinv = ((AccessorToolboxTileEntity) te).getInventory();
						stackInSlot = ((AccessorToolboxInventory) tinv).getFilters().get(scrollSlot);
						inv = tinv;
					} else if (selectedObject instanceof MountedToolboxHolder mt) {
						MountedToolboxInventory mtinv = mt.inventory;
						stackInSlot = mtinv.filters.get(scrollSlot);
						inv = mtinv;
					}
					if (!stackInSlot.isEmpty() && !inv.getStackInSlot(scrollSlot * STACKS_PER_COMPARTMENT)
						.isEmpty())
						break;
				}

				if (state == State.SELECT_BOX)
					if (scrollSlot < toolboxObjects.size())
						break;

				if (state == State.DETACH)
					break;

				scrollSlot -= Mth.sign(delta);
				scrollSlot = (scrollSlot + 8) % 8;
			}
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		int selected = scrollMode ? scrollSlot : hoveredSlot;

		if (button == 0) {
			if (selected == DEPOSIT) {
				onClose();
				AccessorToolboxHandlerClient.setCOOLDOWN(2);
				return true;
			}

			if (state == State.SELECT_BOX && selected >= 0 && selected < toolboxObjects.size()) {
				state = State.SELECT_ITEM;
				selectedObject = toolboxObjects.get(selected);
				return true;
			}

			if (state == State.DETACH || state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
				if (selected == UNEQUIP || selected >= 0) {
					onClose();
					AccessorToolboxHandlerClient.setCOOLDOWN(2);
					return true;
				}
			}
		}

		if (button == 1) {
			if (state == State.SELECT_ITEM && toolboxObjects.size() > 1) {
				state = State.SELECT_BOX;
				return true;
			}

			if (state == State.SELECT_ITEM_UNEQUIP && selected == UNEQUIP) {
				if (toolboxObjects.size() > 1) {
					if (selectedObject instanceof ToolboxTileEntity te)
						AllPackets.channel.sendToServer(new ToolboxEquipPacket(te.getBlockPos(), selected,
							minecraft.player.getInventory().selected));
					else if (selectedObject instanceof MountedToolboxHolder mt)
						CRPackets.channel.sendToServer(new MountedToolboxEquipPacket(mt.parent, selected,
								minecraft.player.getInventory().selected));
					state = State.SELECT_BOX;
					return true;
				}

				onClose();
				AccessorToolboxHandlerClient.setCOOLDOWN(2);
				return true;
			}
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	public boolean keyPressed(int code, int scanCode, int modifiers) {
		KeyMapping[] hotbarBinds = minecraft.options.keyHotbarSlots;
		for (int i = 0; i < hotbarBinds.length && i < 8; i++) {
			if (hotbarBinds[i].matches(code, scanCode)) {

				if (state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
					ItemStack stackInSlot = ItemStack.EMPTY;
					ItemStackHandler inv = null;
					if (selectedObject instanceof ToolboxTileEntity te) {
						ToolboxInventory tinv = ((AccessorToolboxTileEntity) te).getInventory();
						stackInSlot = ((AccessorToolboxInventory) tinv).getFilters().get(i);
						inv = tinv;
					} else if (selectedObject instanceof MountedToolboxHolder mt) {
						MountedToolboxInventory mtinv = mt.inventory;
						stackInSlot = mtinv.filters.get(i);
						inv = mtinv;
					}
					if (stackInSlot.isEmpty() || inv.getStackInSlot(i * STACKS_PER_COMPARTMENT)
						.isEmpty())
						return false;
				}

				if (state == State.SELECT_BOX)
					if (i >= toolboxObjects.size())
						return false;

				scrollMode = true;
				scrollSlot = i;
				mouseClicked(0, 0, 0);
				return true;
			}
		}

		return super.keyPressed(code, scanCode, modifiers);
	}

	@Override
	public boolean keyReleased(int code, int scanCode, int modifiers) {
		InputConstants.Key mouseKey = InputConstants.getKey(code, scanCode);
		if (AllKeys.TOOLBELT.getKeybind()
			.isActiveAndMatches(mouseKey)) {
			onClose();
			return true;
		}
		return super.keyReleased(code, scanCode, modifiers);
	}

}

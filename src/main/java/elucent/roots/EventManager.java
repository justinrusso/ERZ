
package elucent.roots;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import elucent.roots.capability.RootsCapabilityManager;
import elucent.roots.item.IManaRelatedItem;
import elucent.roots.item.ItemCrystalStaff;
import elucent.roots.ritual.RitualPower;
import elucent.roots.ritual.RitualPowerManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventManager {
	Random random = new Random();
	@SubscribeEvent
	public void onBlockHarvested(HarvestDropsEvent event){
		Block block = event.getState().getBlock();
		if (block == Blocks.TALLGRASS){
			if (random.nextInt(ConfigManager.oldRootDropChance) == 0){
				event.getDrops().add(new ItemStack(RegistryManager.oldRoot,1));
			}
		}
		if (block instanceof BlockCrops){
			if (((BlockCrops)block).isMaxAge(event.getState())){
				if (random.nextInt(ConfigManager.verdantSprigDropChance) == 0){
					event.getDrops().add(new ItemStack(RegistryManager.verdantSprig,1));
				}
			}
		}
		if (block == Blocks.NETHER_WART){
			if (((BlockNetherWart) block).getMetaFromState(event.getState()) != 0){
				if (random.nextInt(ConfigManager.infernalStemDropChance) == 0){
					event.getDrops().add(new ItemStack(RegistryManager.infernalStem,1));
				}
			}
		}
		if (block == Blocks.CHORUS_FLOWER){
			if (random.nextInt(ConfigManager.dragonsEyeDropChance) == 0){
				event.getDrops().add(new ItemStack(RegistryManager.dragonsEye,1));
			}
		}
		if (event.getHarvester() != null){
			if (block == Blocks.LEAVES && block.getMetaFromState(event.getState()) == 8){
				if (random.nextInt(ConfigManager.berriesDropChance) == 0){
					event.getDrops().add(new ItemStack(Util.berries.get(random.nextInt(Util.berries.size())),1));
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event){
		if (event.getHand() == EnumHand.MAIN_HAND){
			if (event.getEntityPlayer().getHeldItem(event.getHand()) == null){
				if (event.getEntityPlayer().getEntityData().hasKey(RootsNames.TAG_HAS_RITUAL_POWER)){
					if (event.getEntityPlayer().getEntityData().getInteger(RootsNames.TAG_RITUAL_POWER_COOLDOWN) == 0){
						RitualPowerManager.getPlayerPower(event.getEntityPlayer()).onRightClickBlock(event.getEntityPlayer(), event.getWorld(), event.getPos(), event.getWorld().getBlockState(event.getPos()));
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onRightClickEntity(PlayerInteractEvent.EntityInteract event){
		if (event.getTarget() instanceof EntitySkeleton){
			if (event.getEntityPlayer().getHeldItem(event.getHand()) != null){
				if (event.getEntityPlayer().getHeldItem(event.getHand()).getItem() == RegistryManager.infernalStem){
					System.out.println("WITHERING");
					event.getEntityPlayer().getHeldItem(event.getHand()).stackSize --;
					((EntitySkeleton)event.getTarget()).setSkeletonType(1);
				}
			}
		}
		if (event.getHand() == EnumHand.MAIN_HAND){
			if (event.getEntityPlayer().getHeldItem(event.getHand()) == null){
				if (event.getEntityPlayer().getEntityData().hasKey(RootsNames.TAG_HAS_RITUAL_POWER)){
					if (event.getEntityPlayer().getEntityData().getInteger(RootsNames.TAG_RITUAL_POWER_COOLDOWN) == 0){
						RitualPowerManager.getPlayerPower(event.getEntityPlayer()).onRightClickEntity(event.getEntityPlayer(), event.getWorld(), event.getTarget());
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingTarget(LivingSetAttackTargetEvent event){
		if (event.getTarget() instanceof EntityPlayer && event.getEntityLiving() instanceof EntityMob){
			if (event.getEntity().getEntityData().hasKey(RootsNames.TAG_DONT_TARGET_PLAYERS)){
				if (event.getTarget().getUniqueID().getMostSignificantBits() == event.getEntity().getEntityData().getLong(RootsNames.TAG_DONT_TARGET_PLAYERS)){
					((EntityMob)event.getEntityLiving()).setAttackTarget(null);
					List<EntityLivingBase> targets = event.getEntity().getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(event.getEntity().posX-4.0,event.getEntity().posY-4.0,event.getEntity().posZ-4.0, event.getEntity().posX+4.0,event.getEntity().posY+4.0,event.getEntity().posZ+4.0));
					if (targets.size() > 0){
						((EntityMob)event.getEntityLiving()).setAttackTarget(targets.get(random.nextInt(targets.size())));
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onItemPickup(PlayerEvent.ItemPickupEvent event){
		if (event.pickedUp != null){
			if (event.player != null){
				if (event.pickedUp.getEntityItem().getItem() == RegistryManager.dustPetal){
					if (!event.player.hasAchievement(RegistryManager.achieveDust)){
						PlayerManager.addAchievement(event.player, RegistryManager.achieveDust);
					}
				}
				if (event.pickedUp.getEntityItem().getItem() == Item.getItemFromBlock(RegistryManager.altar)){
					if (!event.player.hasAchievement(RegistryManager.achieveAltar)){
						PlayerManager.addAchievement(event.player, RegistryManager.achieveAltar);
					}
				}
				if (event.pickedUp.getEntityItem().getItem() == Item.getItemFromBlock(RegistryManager.standingStoneT2)){
					if (!event.player.hasAchievement(RegistryManager.achieveStandingStone)){
						PlayerManager.addAchievement(event.player, RegistryManager.achieveStandingStone);
					}
				}
				if (event.pickedUp.getEntityItem().getItem() == RegistryManager.crystalStaff){
					ArrayList<String> spells = new ArrayList<String>();
					spells.add(ItemCrystalStaff.getEffect(event.pickedUp.getEntityItem(),1));
					spells.add(ItemCrystalStaff.getEffect(event.pickedUp.getEntityItem(),2));
					spells.add(ItemCrystalStaff.getEffect(event.pickedUp.getEntityItem(),3));
					spells.add(ItemCrystalStaff.getEffect(event.pickedUp.getEntityItem(),4));
					if (spells.contains("netherwart") && spells.contains("dandelion") && spells.contains("blueorchid") && spells.contains("lilypad")){
						if (!event.player.hasAchievement(RegistryManager.achieveSpellElements)){
							PlayerManager.addAchievement(event.player, RegistryManager.achieveSpellElements);
						}
					}
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void drawQuad(VertexBuffer vertexbuffer, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, int minU, int minV, int maxU, int maxV){
		float f = 0.00390625F;
        float f1 = 0.00390625F;
        vertexbuffer.pos((double)(x1 + 0.0F), (double)(y1 + 0.0F), (double)0).tex((double)((float)(minU + 0) * f), (double)((float)(minV + maxV) * f1)).endVertex();
        vertexbuffer.pos((double)(x2 + 0.0F), (double)(y2 + 0.0F), (double)0).tex((double)((float)(minU + maxU) * f), (double)((float)(minV + maxV) * f1)).endVertex();
        vertexbuffer.pos((double)(x3 + 0.0F), (double)(y3 + 0.0F), (double)0).tex((double)((float)(minU + maxU) * f), (double)((float)(minV + 0) * f1)).endVertex();
        vertexbuffer.pos((double)(x4 + 0.0F), (double)(y4 + 0.0F), (double)0).tex((double)((float)(minU + 0) * f), (double)((float)(minV + 0) * f1)).endVertex();
    }
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGameOverlayRender(RenderGameOverlayEvent.Post e){
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		boolean showBar = false;
		if (player.getHeldItemMainhand() != null){
			if (player.getHeldItemMainhand().getItem() instanceof IManaRelatedItem){
				showBar = true;
			}
		}
		if (player.getHeldItemOffhand() != null){
			if (player.getHeldItemOffhand().getItem() instanceof IManaRelatedItem){
				showBar = true;
			}
		}
		if (player.capabilities.isCreativeMode){
			showBar = false;
		}
		if (showBar){
			if (player.getCapability(RootsCapabilityManager.manaCapability, null).getMaxMana() > 0){
				if (e.getType() == ElementType.TEXT){
					GlStateManager.disableDepth();
					GlStateManager.disableCull();
					GlStateManager.pushMatrix();
					Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("roots:textures/gui/manaBar.png"));
					
					Tessellator tess = Tessellator.getInstance();
					VertexBuffer b = tess.getBuffer();
					int w = e.getResolution().getScaledWidth();
					int h = e.getResolution().getScaledHeight();
					GlStateManager.color(1f, 1f, 1f, 1f);
					
					int manaNumber = Math.round(player.getCapability(RootsCapabilityManager.manaCapability, null).getMana());
					int maxManaNumber = Math.round(player.getCapability(RootsCapabilityManager.manaCapability, null).getMaxMana());
					
					int offsetX = 0;
					
					b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
					while (maxManaNumber > 0){
						this.drawQuad(b, w/2+10+offsetX, h-(ConfigManager.manaBarOffset-9), w/2+19+offsetX, h-(ConfigManager.manaBarOffset-9), w/2+19+offsetX, h-ConfigManager.manaBarOffset, w/2+10+offsetX, h-ConfigManager.manaBarOffset, 0, 0, 9, 9);
						if (maxManaNumber > 4){
							maxManaNumber -= 4;
							offsetX += 8;
						}
						else {
							maxManaNumber = 0;
						}
					}
					offsetX = 0;
					while (manaNumber > 0){
						if (manaNumber > 4){
							this.drawQuad(b, w/2+10+offsetX, h-(ConfigManager.manaBarOffset-9), w/2+19+offsetX, h-(ConfigManager.manaBarOffset-9), w/2+19+offsetX, h-ConfigManager.manaBarOffset, w/2+10+offsetX, h-ConfigManager.manaBarOffset, 0, 16, 9, 9);
							manaNumber -= 4;
							offsetX += 8;
						}
						else {
							if (manaNumber == 4){
								this.drawQuad(b, w/2+10+offsetX, h-(ConfigManager.manaBarOffset-9), w/2+19+offsetX, h-(ConfigManager.manaBarOffset-9), w/2+19+offsetX, h-ConfigManager.manaBarOffset, w/2+10+offsetX, h-ConfigManager.manaBarOffset, 0, 16, 9, 9);
							}
							if (manaNumber == 3){
								this.drawQuad(b, w/2+10+offsetX, h-(ConfigManager.manaBarOffset-9), w/2+19+offsetX, h-(ConfigManager.manaBarOffset-9), w/2+19+offsetX, h-ConfigManager.manaBarOffset, w/2+10+offsetX, h-ConfigManager.manaBarOffset, 16, 16, 9, 9);
							}
							if (manaNumber == 2){
								this.drawQuad(b, w/2+10+offsetX, h-(ConfigManager.manaBarOffset-9), w/2+19+offsetX, h-(ConfigManager.manaBarOffset-9), w/2+19+offsetX, h-ConfigManager.manaBarOffset, w/2+10+offsetX, h-ConfigManager.manaBarOffset, 32, 16, 9, 9);
							}
							if (manaNumber == 1){
								this.drawQuad(b, w/2+10+offsetX, h-(ConfigManager.manaBarOffset-9), w/2+19+offsetX, h-(ConfigManager.manaBarOffset-9), w/2+19+offsetX, h-ConfigManager.manaBarOffset, w/2+10+offsetX, h-ConfigManager.manaBarOffset, 48, 16, 9, 9);
							}
							manaNumber = 0;
						}
					}
					tess.draw();
					
					GlStateManager.popMatrix();
					GlStateManager.enableCull();
					GlStateManager.enableDepth();
				}
			}
		}
		/*System.out.println(player.getEntityData());
		boolean showPowerBar = player.getEntityData().hasKey(RootsNames.TAG_HAS_RITUAL_POWER);
		if (player.capabilities.isCreativeMode){
			showPowerBar = false;
		}
		if (showPowerBar){
			if (e.getType() == ElementType.TEXT){
				GlStateManager.disableDepth();
				GlStateManager.disableCull();
				GlStateManager.pushMatrix();
				Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("roots:textures/gui/manaBar.png"));
				
				Tessellator tess = Tessellator.getInstance();
				VertexBuffer b = tess.getBuffer();
				int w = e.getResolution().getScaledWidth();
				int h = e.getResolution().getScaledHeight();
				GlStateManager.color(1f, 1f, 1f, 1f);
				int texOffset = 64;
				RitualPower playerPower = RitualPowerManager.getPlayerPower(player);
				if (playerPower.name == "grow"){
					texOffset = 96;
				}
				if (playerPower.name == "breed"){
					texOffset = 128;
				}
				if (playerPower.name == "flare"){
					texOffset = 160;
				}
				int powerNumber = player.getEntityData().getInteger(playerPower.getTagName());
				int maxPowerNumber = 20;
				
				int offsetX = 0;
				
				b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
				while (maxPowerNumber > 0){
					this.drawQuad(b, w/2+10+offsetX, h-(ConfigManager.manaBarOffset-19), w/2+19+offsetX, h-(ConfigManager.manaBarOffset-19), w/2+19+offsetX, h-ConfigManager.manaBarOffset-10, w/2+10+offsetX, h-ConfigManager.manaBarOffset-10, texOffset, 0, 9, 9);
					if (maxPowerNumber > 2){
						maxPowerNumber -= 2;
						offsetX += 8;
					}
					else {
						maxPowerNumber = 0;
					}
				}
				offsetX = 0;
				while (powerNumber > 0){
					if (powerNumber > 2){
						this.drawQuad(b, w/2+10+offsetX, h-(ConfigManager.manaBarOffset-19), w/2+19+offsetX, h-(ConfigManager.manaBarOffset-19), w/2+19+offsetX, h-ConfigManager.manaBarOffset-10, w/2+10+offsetX, h-ConfigManager.manaBarOffset-10, texOffset, 16, 9, 9);
						powerNumber -= 2;
						offsetX += 8;
					}
					else {
						if (powerNumber == 2){
							this.drawQuad(b, w/2+10+offsetX, h-(ConfigManager.manaBarOffset-19), w/2+19+offsetX, h-(ConfigManager.manaBarOffset-19), w/2+19+offsetX, h-ConfigManager.manaBarOffset-10, w/2+10+offsetX, h-ConfigManager.manaBarOffset-10, texOffset, 16, 9, 9);
						}
						if (powerNumber == 1){
							this.drawQuad(b, w/2+10+offsetX, h-(ConfigManager.manaBarOffset-19), w/2+19+offsetX, h-(ConfigManager.manaBarOffset-19), w/2+19+offsetX, h-ConfigManager.manaBarOffset-10, w/2+10+offsetX, h-ConfigManager.manaBarOffset-10, texOffset+16, 16, 9, 9);
						}
						powerNumber = 0;
					}
				}
				tess.draw();
				
				GlStateManager.popMatrix();
				GlStateManager.enableCull();
				GlStateManager.enableDepth();
			}
		}*/
	}
	
	@SubscribeEvent
	public void onItemCraft(ItemCraftedEvent event){
		if (event.player != null){
			if (event.crafting != null){
				if (event.crafting.getItem() == Item.getItemFromBlock(RegistryManager.altar)){
					if (!event.player.hasAchievement(RegistryManager.achieveAltar)){
						PlayerManager.addAchievement(event.player, RegistryManager.achieveAltar);
					}
				}
				if (event.crafting.getItem() == Item.getItemFromBlock(RegistryManager.standingStoneT2)){
					if (!event.player.hasAchievement(RegistryManager.achieveStandingStone)){
						PlayerManager.addAchievement(event.player, RegistryManager.achieveStandingStone);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingTick(LivingUpdateEvent event){
		if (event.getEntityLiving() instanceof EntityPlayer){
			if (event.getEntityLiving().getEntityData().hasKey(RootsNames.TAG_RITUAL_POWER_COOLDOWN)){
				if (event.getEntityLiving().getEntityData().getInteger(RootsNames.TAG_RITUAL_POWER_COOLDOWN) > 0){
					event.getEntityLiving().getEntityData().setInteger(RootsNames.TAG_RITUAL_POWER_COOLDOWN, event.getEntityLiving().getEntityData().getInteger(RootsNames.TAG_RITUAL_POWER_COOLDOWN)-1);
				}
				if (event.getEntityLiving().getEntityData().getInteger(RootsNames.TAG_RITUAL_POWER_COOLDOWN) < 0){
					event.getEntityLiving().getEntityData().setInteger(RootsNames.TAG_RITUAL_POWER_COOLDOWN, 0);
				}
			}
			if (event.getEntityLiving().ticksExisted % 5 == 0){
				if (event.getEntityLiving().hasCapability(RootsCapabilityManager.manaCapability, null)){
					event.getEntityLiving().getCapability(RootsCapabilityManager.manaCapability, null).setMana(event.getEntityLiving().getCapability(RootsCapabilityManager.manaCapability, null).getMana()+1.0f);
				}
			}
		}
		if (event.getEntityLiving().getEntityData().hasKey(RootsNames.TAG_TRACK_TICKS)){
			if (event.getEntityLiving().getEntityData().hasKey(RootsNames.TAG_SPELL_SKIP_TICKS)){
				if (event.getEntityLiving().getEntityData().getInteger(RootsNames.TAG_SPELL_SKIP_TICKS) > 0){
					if (event.getEntityLiving().getHealth() <= 0){
						if (event.getEntityLiving().getLastAttacker() instanceof EntityPlayer){
							if (!((EntityPlayer)event.getEntityLiving().getLastAttacker()).hasAchievement(RegistryManager.achieveTimeStop)){
								PlayerManager.addAchievement((EntityPlayer)event.getEntityLiving().getLastAttacker(), RegistryManager.achieveTimeStop);
							}
						}
					}
					event.getEntityLiving().getEntityData().setInteger(RootsNames.TAG_SPELL_SKIP_TICKS, event.getEntityLiving().getEntityData().getInteger(RootsNames.TAG_SPELL_SKIP_TICKS)-1);
					if (event.getEntityLiving().getEntityData().getInteger(RootsNames.TAG_SPELL_SKIP_TICKS) <= 0){
						event.getEntityLiving().getEntityData().removeTag(RootsNames.TAG_SPELL_SKIP_TICKS);
						Util.decrementTickTracking(event.getEntityLiving());
					}
					event.setCanceled(true);
				}
			}
		}
		if(event.getEntityLiving().getEntityData().hasKey("RMOD_icy")){
			EntityLivingBase entity = event.getEntityLiving();
			BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
			if(!entity.getEntityWorld().isAirBlock(pos.down())){
				if(entity.getEntityWorld().getBlockState(pos.down()) == Blocks.WATER.getDefaultState()){
					entity.getEntityWorld().setBlockState(pos.down(), Blocks.ICE.getDefaultState());
				} else{
					entity.getEntityWorld().setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState());
				}
			}	
			entity.getEntityData().setInteger("RMOD_icy", event.getEntityLiving().getEntityData().getInteger("RMOD_icy")-1);
			if(entity.getEntityData().getInteger("RMOD_icy") <= 0){
				entity.getEntityData().removeTag("RMOD_icy");
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingDrops(LivingDropsEvent event){
		if (event.getEntityLiving().getEntityData().hasKey(RootsNames.TAG_SKIP_ITEM_DROPS)){
			if (!event.getEntityLiving().getEntityData().getBoolean(RootsNames.TAG_SKIP_ITEM_DROPS)){
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingXP(LivingExperienceDropEvent event){
		if (event.getEntityLiving().getEntityData().hasKey(RootsNames.TAG_SKIP_ITEM_DROPS)){
			if (!event.getEntityLiving().getEntityData().getBoolean(RootsNames.TAG_SKIP_ITEM_DROPS)){
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingDamage(LivingHurtEvent event){
		if (event.getEntityLiving().getEntityData().hasKey(RootsNames.TAG_SPELL_VULNERABILITY)){
			event.setAmount((float) (event.getAmount()*(1.0+event.getEntityLiving().getEntityData().getDouble("RMOD_vuln"))));
			event.getEntityLiving().getEntityData().removeTag(RootsNames.TAG_SPELL_VULNERABILITY);
		}
		
		if (event.getEntityLiving().getEntityData().hasKey(RootsNames.TAG_SPELL_THORNS_DAMAGE) && event.getSource().getEntity() instanceof EntityLivingBase){
			((EntityLivingBase)event.getSource().getEntity()).attackEntityFrom(DamageSource.cactus, event.getEntityLiving().getEntityData().getFloat(RootsNames.TAG_SPELL_THORNS_DAMAGE));
			event.getEntityLiving().getEntityData().removeTag(RootsNames.TAG_SPELL_THORNS_DAMAGE);
			Util.decrementTickTracking(event.getEntityLiving());
		}
		
		if(event.getEntityLiving() instanceof EntityPlayer){
			EntityPlayer player = (EntityPlayer)event.getEntity();
			if(player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() == RegistryManager.engravedSword){
				ItemStack sword = player.inventory.getCurrentItem();
				if(sword.hasTagCompound() && sword.getTagCompound().hasKey("shadowstep")){
					int stepLvl = sword.getTagCompound().getInteger("shadowstep");
					double chance = (double)stepLvl * 12.5;
					if(random.nextInt(100) < chance){
						event.setCanceled(true);
					}
				}
			}
		}
		
		if(event.getSource().getEntity() instanceof EntityPlayer){
			if(!event.getEntity().getEntityWorld().isRemote){
				EntityPlayer player = (EntityPlayer)event.getSource().getEntity();
				if(player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() == RegistryManager.engravedSword){
					ItemStack sword = player.inventory.getCurrentItem();
					if(sword.hasTagCompound() && sword.getTagCompound().hasKey("aquatic")){
						int aquaLvl = sword.getTagCompound().getInteger("aquatic");
						float amount = aquaLvl * 0.5f;
						event.getEntity().attackEntityFrom(DamageSource.drown, amount);
					}
					if((sword.hasTagCompound() && sword.getTagCompound().hasKey("holy")) && event.getEntityLiving().getCreatureAttribute() == EnumCreatureAttribute.UNDEAD){
						int holyLvl = sword.getTagCompound().getInteger("holy");
						float amount = holyLvl * 1.5f;
						float currentAmount = event.getAmount();
						event.setAmount(currentAmount + amount);
					}
					if(sword.hasTagCompound() && sword.getTagCompound().hasKey("spikes")){
						int spikeLvl = sword.getTagCompound().getInteger("spikes");
						float amount = spikeLvl;
						float currentAmount = event.getAmount();
						event.setAmount(currentAmount + amount);
					}
				}
			}
		}	
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTextureStitch(TextureStitchEvent event){
		ResourceLocation magicParticleRL = new ResourceLocation("roots:entity/magicParticle");
		event.getMap().registerSprite(magicParticleRL);
	}
}

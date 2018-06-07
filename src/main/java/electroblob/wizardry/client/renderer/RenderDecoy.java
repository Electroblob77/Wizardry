package electroblob.wizardry.client.renderer;

import java.util.UUID;

import org.lwjgl.opengl.GL11;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.client.model.ModelWizard;
import electroblob.wizardry.entity.living.EntityDecoy;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.entity.living.EntityWizard;
import net.minecraft.block.Block;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

@SideOnly(Side.CLIENT)
public class RenderDecoy extends RenderBiped
{
    private static final ResourceLocation steveTextures = new ResourceLocation("textures/entity/steve.png");
    public ModelBiped modelBipedMain;
    public ModelBiped modelArmorChestplate;
    public ModelBiped modelArmor;
    public ModelWizard modelWizard;

    public RenderDecoy()
    {
        super(new ModelBiped(0.0F), 0.5F);
        this.modelBipedMain = (ModelBiped)this.mainModel;
        this.modelArmorChestplate = new ModelBiped(1.0F);
        this.modelArmor = new ModelBiped(0.5F);
        this.modelWizard = new ModelWizard();
    }

    /**
     * Queries whether should render the specified pass or not.
     */
    protected int shouldRenderPass(EntityDecoy decoy, int pass, float partialTickTime)
    {
    	if(decoy.getCaster() instanceof EntityPlayer){
	    	
	        ItemStack itemstack = ((AbstractClientPlayer)decoy.getCaster()).inventory.armorItemInSlot(3 - pass);
	
	        if (itemstack != null)
	        {
	            Item item = itemstack.getItem();
	
	            if (item instanceof ItemArmor)
	            {
	                ItemArmor itemarmor = (ItemArmor)item;
	                this.bindTexture(RenderBiped.getArmorResource(decoy, itemstack, pass, null));
	                ModelBiped modelbiped = pass == 2 ? this.modelArmor : this.modelArmorChestplate;
	                modelbiped.bipedHead.showModel = pass == 0;
	                modelbiped.bipedHeadwear.showModel = pass == 0;
	                modelbiped.bipedBody.showModel = pass == 1 || pass == 2;
	                modelbiped.bipedRightArm.showModel = pass == 1;
	                modelbiped.bipedLeftArm.showModel = pass == 1;
	                modelbiped.bipedRightLeg.showModel = pass == 2 || pass == 3;
	                modelbiped.bipedLeftLeg.showModel = pass == 2 || pass == 3;
	                modelbiped = net.minecraftforge.client.ForgeHooksClient.getArmorModel(decoy, itemstack, pass, modelbiped);
	                this.setRenderPassModel(modelbiped);
	                modelbiped.onGround = this.mainModel.onGround;
	                modelbiped.isRiding = this.mainModel.isRiding;
	                modelbiped.isChild = this.mainModel.isChild;
	
	                //Move outside if to allow for more then just CLOTH
	                int j = itemarmor.getColor(itemstack);
	                if (j != -1)
	                {
	                    float f1 = (float)(j >> 16 & 255) / 255.0F;
	                    float f2 = (float)(j >> 8 & 255) / 255.0F;
	                    float f3 = (float)(j & 255) / 255.0F;
	                    GL11.glColor3f(f1, f2, f3);
	
	                    if (itemstack.isItemEnchanted())
	                    {
	                        return 31;
	                    }
	
	                    return 16;
	                }
	
	                GL11.glColor3f(1.0F, 1.0F, 1.0F);
	
	                if (itemstack.isItemEnchanted())
	                {
	                    return 15;
	                }
	
	                return 1;
	            }
	        }
    	}else{
    		
    		return super.shouldRenderPass(decoy.getCaster(), pass, partialTickTime);
    	}

        return -1;
    }

    protected void func_82408_c(EntityDecoy decoy, int p_82408_2_, float p_82408_3_)
    {
        ItemStack itemstack = ((AbstractClientPlayer)decoy.getCaster()).inventory.armorItemInSlot(3 - p_82408_2_);

        if (itemstack != null)
        {
            Item item = itemstack.getItem();

            if (item instanceof ItemArmor)
            {
                this.bindTexture(RenderBiped.getArmorResource(decoy, itemstack, p_82408_2_, "overlay"));
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
            }
        }
    }

    public void doRender(EntityDecoy decoy, double x, double y, double z, float yaw, float pitch)
    {
    	if(decoy.getCaster() instanceof EntityPlayer){
    		
    		this.mainModel = this.modelBipedMain;
    		
	        GL11.glColor3f(1.0F, 1.0F, 1.0F);
	        ItemStack itemstack = ((AbstractClientPlayer)decoy.getCaster()).inventory.getCurrentItem();
	        this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = itemstack != null ? 1 : 0;
	
	        if (itemstack != null && ((AbstractClientPlayer)decoy.getCaster()).getItemInUseCount() > 0)
	        {
	            EnumAction enumaction = itemstack.getItemUseAction();
	
	            if (enumaction == EnumAction.block)
	            {
	                this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = 3;
	            }
	            else if (enumaction == EnumAction.bow)
	            {
	                this.modelArmorChestplate.aimedBow = this.modelArmor.aimedBow = this.modelBipedMain.aimedBow = true;
	            }
	        }
	
	        this.modelArmorChestplate.isSneak = this.modelArmor.isSneak = this.modelBipedMain.isSneak = decoy.isSneaking();
	        double d3 = y - (double)decoy.yOffset;
	
	        super.doRender((EntityLivingBase)decoy, x, d3, z, yaw, pitch);
	        this.modelArmorChestplate.aimedBow = this.modelArmor.aimedBow = this.modelBipedMain.aimedBow = false;
	        this.modelArmorChestplate.isSneak = this.modelArmor.isSneak = this.modelBipedMain.isSneak = false;
	        this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = 0;
    	
    	}else{
    		
    		this.mainModel = this.modelWizard;
    		
    		// Since wizards always have a wand, I don't see any point in checking
    		this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = 1;
    		
    		super.doRender(decoy, x, y, z, yaw, pitch);
    		
    	}
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityDecoy decoy)
    {
    	//System.out.println(decoy.getCaster());
    	
        if(decoy.getCaster() instanceof EntityWizard){
        	return RenderWizard.textures[((EntityWizard)decoy.getCaster()).textureIndex];
        }else if(decoy.getCaster() instanceof EntityEvilWizard){
        	return RenderEvilWizard.textures[((EntityEvilWizard)decoy.getCaster()).textureIndex];
        }else if(decoy.getCaster() instanceof EntityPlayer){
        	return ((AbstractClientPlayer) decoy.getCaster()).getLocationSkin();
        }else{
        	return steveTextures;
        }
    }

    protected void renderEquippedItems(EntityDecoy decoy, float p_77029_2_)
    {
    	if(decoy.getCaster() instanceof EntityPlayer){
	    	
	    	GL11.glColor3f(1.0F, 1.0F, 1.0F);
	        super.renderEquippedItems(decoy, p_77029_2_);
	        super.renderArrowsStuckInEntity(decoy, p_77029_2_);
	        ItemStack itemstack = ((AbstractClientPlayer)decoy.getCaster()).inventory.armorItemInSlot(3);
	
	        if (itemstack != null)
	        {
	            GL11.glPushMatrix();
	            this.modelBipedMain.bipedHead.postRender(0.0625F);
	            float f1;
	
	            if (itemstack.getItem() instanceof ItemBlock)
	            {
	                net.minecraftforge.client.IItemRenderer customRenderer = net.minecraftforge.client.MinecraftForgeClient.getItemRenderer(itemstack, net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED);
	                boolean is3D = (customRenderer != null && customRenderer.shouldUseRenderHelper(net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED, itemstack, net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D));
	
	                if (is3D || RenderBlocks.renderItemIn3d(Block.getBlockFromItem(itemstack.getItem()).getRenderType()))
	                {
	                    f1 = 0.625F;
	                    GL11.glTranslatef(0.0F, -0.25F, 0.0F);
	                    GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
	                    GL11.glScalef(f1, -f1, -f1);
	                }
	
	                this.renderManager.itemRenderer.renderItem(decoy, itemstack, 0);
	            }
	            else if (itemstack.getItem() == Items.skull)
	            {
	                f1 = 1.0625F;
	                GL11.glScalef(f1, -f1, -f1);
	                GameProfile gameprofile = null;
	
	                if (itemstack.hasTagCompound())
	                {
	                    NBTTagCompound nbttagcompound = itemstack.getTagCompound();
	
	                    if (nbttagcompound.hasKey("SkullOwner", 10))
	                    {
	                        gameprofile = NBTUtil.func_152459_a(nbttagcompound.getCompoundTag("SkullOwner"));
	                    }
	                    else if (nbttagcompound.hasKey("SkullOwner", 8) && !StringUtils.isNullOrEmpty(nbttagcompound.getString("SkullOwner")))
	                    {
	                        gameprofile = new GameProfile((UUID)null, nbttagcompound.getString("SkullOwner"));
	                    }
	                }
	
	                TileEntitySkullRenderer.field_147536_b.func_152674_a(-0.5F, 0.0F, -0.5F, 1, 180.0F, itemstack.getItemDamage(), gameprofile);
	            }
	
	            GL11.glPopMatrix();
	        }
	
	        float f2;
	
	        if (decoy.getCommandSenderName().equals("deadmau5") && ((AbstractClientPlayer)decoy.getCaster()).func_152123_o())
	        {
	            this.bindTexture(((AbstractClientPlayer)decoy.getCaster()).getLocationSkin());
	
	            for (int j = 0; j < 2; ++j)
	            {
	                float f9 = decoy.prevRotationYaw + (decoy.rotationYaw - decoy.prevRotationYaw) * p_77029_2_ - (decoy.prevRenderYawOffset + (decoy.renderYawOffset - decoy.prevRenderYawOffset) * p_77029_2_);
	                float f10 = decoy.prevRotationPitch + (decoy.rotationPitch - decoy.prevRotationPitch) * p_77029_2_;
	                GL11.glPushMatrix();
	                GL11.glRotatef(f9, 0.0F, 1.0F, 0.0F);
	                GL11.glRotatef(f10, 1.0F, 0.0F, 0.0F);
	                GL11.glTranslatef(0.375F * (float)(j * 2 - 1), 0.0F, 0.0F);
	                GL11.glTranslatef(0.0F, -0.375F, 0.0F);
	                GL11.glRotatef(-f10, 1.0F, 0.0F, 0.0F);
	                GL11.glRotatef(-f9, 0.0F, 1.0F, 0.0F);
	                f2 = 1.3333334F;
	                GL11.glScalef(f2, f2, f2);
	                this.modelBipedMain.renderEars(0.0625F);
	                GL11.glPopMatrix();
	            }
	        }
	
	        boolean flag = ((AbstractClientPlayer)decoy.getCaster()).func_152122_n();
	        float f4;
	
	        if (flag && !decoy.isInvisible() && !((AbstractClientPlayer)decoy.getCaster()).getHideCape())
	        {
	            this.bindTexture(((AbstractClientPlayer)decoy.getCaster()).getLocationCape());
	            GL11.glPushMatrix();
	            GL11.glTranslatef(0.0F, 0.0F, 0.125F);
	            double d3 = ((AbstractClientPlayer)decoy.getCaster()).field_71091_bM + (((AbstractClientPlayer)decoy.getCaster()).field_71094_bP - ((AbstractClientPlayer)decoy.getCaster()).field_71091_bM) * (double)p_77029_2_ - (decoy.prevPosX + (decoy.posX - decoy.prevPosX) * (double)p_77029_2_);
	            double d4 = ((AbstractClientPlayer)decoy.getCaster()).field_71096_bN + (((AbstractClientPlayer)decoy.getCaster()).field_71095_bQ - ((AbstractClientPlayer)decoy.getCaster()).field_71096_bN) * (double)p_77029_2_ - (decoy.prevPosY + (decoy.posY - decoy.prevPosY) * (double)p_77029_2_);
	            double d0 = ((AbstractClientPlayer)decoy.getCaster()).field_71097_bO + (((AbstractClientPlayer)decoy.getCaster()).field_71085_bR - ((AbstractClientPlayer)decoy.getCaster()).field_71097_bO) * (double)p_77029_2_ - (decoy.prevPosZ + (decoy.posZ - decoy.prevPosZ) * (double)p_77029_2_);
	            f4 = decoy.prevRenderYawOffset + (decoy.renderYawOffset - decoy.prevRenderYawOffset) * p_77029_2_;
	            double d1 = (double)MathHelper.sin(f4 * (float)Math.PI / 180.0F);
	            double d2 = (double)(-MathHelper.cos(f4 * (float)Math.PI / 180.0F));
	            float f5 = (float)d4 * 10.0F;
	
	            if (f5 < -6.0F)
	            {
	                f5 = -6.0F;
	            }
	
	            if (f5 > 32.0F)
	            {
	                f5 = 32.0F;
	            }
	
	            float f6 = (float)(d3 * d1 + d0 * d2) * 100.0F;
	            float f7 = (float)(d3 * d2 - d0 * d1) * 100.0F;
	
	            if (f6 < 0.0F)
	            {
	                f6 = 0.0F;
	            }
	
	            float f8 = ((AbstractClientPlayer)decoy.getCaster()).prevCameraYaw + (((AbstractClientPlayer)decoy.getCaster()).cameraYaw - ((AbstractClientPlayer)decoy.getCaster()).prevCameraYaw) * p_77029_2_;
	            f5 += MathHelper.sin((decoy.prevDistanceWalkedModified + (decoy.distanceWalkedModified - decoy.prevDistanceWalkedModified) * p_77029_2_) * 6.0F) * 32.0F * f8;
	
	            if (decoy.isSneaking())
	            {
	                f5 += 25.0F;
	            }
	
	            GL11.glRotatef(6.0F + f6 / 2.0F + f5, 1.0F, 0.0F, 0.0F);
	            GL11.glRotatef(f7 / 2.0F, 0.0F, 0.0F, 1.0F);
	            GL11.glRotatef(-f7 / 2.0F, 0.0F, 1.0F, 0.0F);
	            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
	            this.modelBipedMain.renderCloak(0.0625F);
	            GL11.glPopMatrix();
	        }
	
	        ItemStack itemstack1 = ((AbstractClientPlayer)decoy.getCaster()).inventory.getCurrentItem();
	
	        if (itemstack1 != null)
	        {
	            GL11.glPushMatrix();
	            this.modelBipedMain.bipedRightArm.postRender(0.0625F);
	            GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);
	
	            EnumAction enumaction = null;
	
	            if (((AbstractClientPlayer)decoy.getCaster()).getItemInUseCount() > 0)
	            {
	                enumaction = itemstack1.getItemUseAction();
	            }
	
	            net.minecraftforge.client.IItemRenderer customRenderer = net.minecraftforge.client.MinecraftForgeClient.getItemRenderer(itemstack1, net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED);
	            boolean is3D = (customRenderer != null && customRenderer.shouldUseRenderHelper(net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED, itemstack1, net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D));
	
	            if (is3D || itemstack1.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(itemstack1.getItem()).getRenderType()))
	            {
	                f2 = 0.5F;
	                GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
	                f2 *= 0.75F;
	                GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
	                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
	                GL11.glScalef(-f2, -f2, f2);
	            }
	            else if (itemstack1.getItem() == Items.bow)
	            {
	                f2 = 0.625F;
	                GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
	                GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
	                GL11.glScalef(f2, -f2, f2);
	                GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
	                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
	            }
	            else if (itemstack1.getItem().isFull3D())
	            {
	                f2 = 0.625F;
	
	                if (itemstack1.getItem().shouldRotateAroundWhenRendering())
	                {
	                    GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
	                    GL11.glTranslatef(0.0F, -0.125F, 0.0F);
	                }
	
	                if (((AbstractClientPlayer)decoy.getCaster()).getItemInUseCount() > 0 && enumaction == EnumAction.block)
	                {
	                    GL11.glTranslatef(0.05F, 0.0F, -0.1F);
	                    GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
	                    GL11.glRotatef(-10.0F, 1.0F, 0.0F, 0.0F);
	                    GL11.glRotatef(-60.0F, 0.0F, 0.0F, 1.0F);
	                }
	
	                GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
	                GL11.glScalef(f2, -f2, f2);
	                GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
	                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
	            }
	            else
	            {
	                f2 = 0.375F;
	                GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
	                GL11.glScalef(f2, f2, f2);
	                GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
	                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
	                GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
	            }
	
	            float f3;
	            int k;
	            float f12;
	
	            if (itemstack1.getItem().requiresMultipleRenderPasses())
	            {
	                for (k = 0; k < itemstack1.getItem().getRenderPasses(itemstack1.getItemDamage()); ++k)
	                {
	                    int i = itemstack1.getItem().getColorFromItemStack(itemstack1, k);
	                    f12 = (float)(i >> 16 & 255) / 255.0F;
	                    f3 = (float)(i >> 8 & 255) / 255.0F;
	                    f4 = (float)(i & 255) / 255.0F;
	                    GL11.glColor4f(f12, f3, f4, 1.0F);
	                    this.renderManager.itemRenderer.renderItem(decoy, itemstack1, k);
	                }
	            }
	            else
	            {
	                k = itemstack1.getItem().getColorFromItemStack(itemstack1, 0);
	                float f11 = (float)(k >> 16 & 255) / 255.0F;
	                f12 = (float)(k >> 8 & 255) / 255.0F;
	                f3 = (float)(k & 255) / 255.0F;
	                GL11.glColor4f(f11, f12, f3, 1.0F);
	                this.renderManager.itemRenderer.renderItem(decoy, itemstack1, 0);
	            }
	
	            GL11.glPopMatrix();
	        }
	        
    	}else{
    		super.renderEquippedItems(decoy, p_77029_2_);
    	}
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(EntityDecoy p_77041_1_, float p_77041_2_)
    {
        float f1 = 0.9375F;
        GL11.glScalef(f1, f1, f1);
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(EntityLivingBase p_77041_1_, float p_77041_2_)
    {
        this.preRenderCallback((EntityDecoy)p_77041_1_, p_77041_2_);
    }

    protected void func_82408_c(EntityLivingBase p_82408_1_, int p_82408_2_, float p_82408_3_)
    {
        this.func_82408_c((EntityDecoy)p_82408_1_, p_82408_2_, p_82408_3_);
    }

    /**
     * Queries whether should render the specified pass or not.
     */
    protected int shouldRenderPass(EntityLivingBase p_77032_1_, int p_77032_2_, float p_77032_3_)
    {
        return this.shouldRenderPass((EntityDecoy)p_77032_1_, p_77032_2_, p_77032_3_);
    }

    protected void renderEquippedItems(EntityLivingBase p_77029_1_, float p_77029_2_)
    {
        this.renderEquippedItems((EntityDecoy)p_77029_1_, p_77029_2_);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(EntityLivingBase p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
        this.doRender((EntityDecoy)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(Entity p_110775_1_)
    {
        return this.getEntityTexture((EntityDecoy)p_110775_1_);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
        this.doRender((EntityDecoy)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }
}
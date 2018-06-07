package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.client.model.ModelWizard;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class RenderEvilWizard extends RenderBiped
{
    static final ResourceLocation[] textures = new ResourceLocation[6];

    /** Model of the EvilWizard. */
    protected ModelWizard villagerModel;

    public RenderEvilWizard()
    {
        super(new ModelWizard(), 0.5F);
        this.villagerModel = (ModelWizard)this.mainModel;
        for(int i=0;i<6;i++){
			textures[i] = new ResourceLocation("wizardry:textures/entity/evil_wizard_" + i + ".png");
		}
    }

    /**
     * Determines wether Villager Render pass or not.
     */
    protected int shouldVillagerRenderPass(EntityEvilWizard par1EntityEvilWizard, int par2, float par3)
    {
        return -1;
    }

    public void renderVillager(EntityEvilWizard par1EntityEvilWizard, double par2, double par4, double par6, float par8, float par9)
    {
        super.doRender(par1EntityEvilWizard, par2, par4, par6, par8, par9);
    }

    protected ResourceLocation func_110902_a(EntityEvilWizard par1EntityEvilWizard)
    {
        return textures[par1EntityEvilWizard.textureIndex];
    }

    protected void renderVillagerEquipedItems(EntityEvilWizard par1EntityEvilWizard, float par2)
    {
        super.renderEquippedItems(par1EntityEvilWizard, par2);
    }

    protected void preRenderVillager(EntityEvilWizard par1EntityEvilWizard, float par2)
    {
        float f1 = 0.9375F;
        this.shadowSize = 0.5F;

        GL11.glScalef(f1, f1, f1);
    }

    public void doRenderLiving(EntityLiving par1EntityLiving, double par2, double par4, double par6, float par8, float par9)
    {
        this.renderVillager((EntityEvilWizard)par1EntityLiving, par2, par4, par6, par8, par9);
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(EntityLivingBase par1EntityLivingBase, float par2)
    {
        this.preRenderVillager((EntityEvilWizard)par1EntityLivingBase, par2);
    }

    /**
     * Queries whether should render the specified pass or not.
     */
    protected int shouldRenderPass(EntityLivingBase par1EntityLivingBase, int par2, float par3)
    {
    	// TODO: Is this the right method name now? There are several with correct parameters. This one may end up being recursive!
    	return this.shouldRenderPass((EntityLiving)par1EntityLivingBase, par2, par3);
        //return this.shouldVillagerRenderPass((EntityEvilWizard)par1EntityLivingBase, par2, par3);
    }

    protected void renderEquippedItems(EntityLivingBase par1EntityLivingBase, float par2)
    {
        this.renderVillagerEquipedItems((EntityEvilWizard)par1EntityLivingBase, par2);
    }

    public void renderPlayer(EntityLivingBase par1EntityLivingBase, double par2, double par4, double par6, float par8, float par9)
    {
        this.renderVillager((EntityEvilWizard)par1EntityLivingBase, par2, par4, par6, par8, par9);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(Entity par1Entity)
    {
        return this.func_110902_a((EntityEvilWizard)par1Entity);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.renderVillager((EntityEvilWizard)par1Entity, par2, par4, par6, par8, par9);
    }
}

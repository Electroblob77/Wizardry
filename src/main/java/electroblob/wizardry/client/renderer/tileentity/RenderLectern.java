package electroblob.wizardry.client.renderer.tileentity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.tileentity.TileEntityLectern;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderLectern extends TileEntitySpecialRenderer<TileEntityLectern> {

	private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/lectern_book.png");
	private final ModelBook modelBook = new ModelBook();

	@Override
	public void render(TileEntityLectern te, double x, double y, double z, float partialTicks, int destroyStage, float alpha){

		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x + 0.5F, (float)y + 1, (float)z + 0.5F);
		GlStateManager.rotate(90 - te.getWorld().getBlockState(te.getPos()).getValue(BlockHorizontal.FACING).getHorizontalAngle(), 0, 1, 0);

		float time = (float)te.ticksExisted + partialTicks;

		float spread = te.bookSpreadPrev + (te.bookSpread - te.bookSpreadPrev) * partialTicks;

		GlStateManager.translate(0, 0.12, 0);
		if(spread > 0.3) GlStateManager.translate(0, MathHelper.sin(time * 0.1F) * 0.01F, 0);

		GlStateManager.rotate(112.5F, 0, 0, 1);

		GlStateManager.translate(0, 0.04 + (1 - spread) * 0.09, (1 - spread) * -0.1875);

		GlStateManager.rotate((1 - spread) * -90, 0, 1, 0);

		this.bindTexture(BOOK_TEXTURE);

		float f3 = te.pageFlipPrev + (te.pageFlip - te.pageFlipPrev) * partialTicks + 0.25F;
		float f4 = te.pageFlipPrev + (te.pageFlip - te.pageFlipPrev) * partialTicks + 0.75F;
		f3 = (f3 - (float)MathHelper.fastFloor(f3)) * 1.6F - 0.3F;
		f4 = (f4 - (float)MathHelper.fastFloor(f4)) * 1.6F - 0.3F;

		f3 = MathHelper.clamp(f3, 0, 1);
		f4 = MathHelper.clamp(f4, 0, 1);

		GlStateManager.enableCull();
		this.modelBook.render(null, time, f3, f4, spread, 0.0F, 0.0625F);
		GlStateManager.popMatrix();

	}

}

package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;

public class StormArcRenderState extends EntityRenderState {

	public long seed;
	public Vec3 endOffset = Vec3.ZERO;
	public float boltLength;
}

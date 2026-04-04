package spookipup.uniquemobs.renderer;

import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.world.phys.Vec3;

public class DeltaGhastRenderState extends GhastRenderState {

	public boolean isFiring;
	public int chargeTicks;
	public Vec3 beamDirection;
	public float eyeHeight;
	public float beamLength;
}

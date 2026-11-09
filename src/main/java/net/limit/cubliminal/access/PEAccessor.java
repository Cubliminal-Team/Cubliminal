package net.limit.cubliminal.access;

import net.limit.cubliminal.event.noclip.NoclipEngine;
import net.limit.cubliminal.event.sanity.SanityManager;

public interface PEAccessor {

	NoclipEngine getNoclipEngine();

	SanityManager getSanityManager();

}

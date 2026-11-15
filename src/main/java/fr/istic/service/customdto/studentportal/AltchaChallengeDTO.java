package fr.istic.service.customdto.studentportal;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** ALTCHA challenge, in the format expected by the widget (protocol v1). */
@RegisterForReflection
@Schema(description = "ALTCHA proof-of-work challenge for the student portal.")
public class AltchaChallengeDTO {
    public String algorithm;
    public String challenge;
    public int maxnumber;
    public String salt;
    public String signature;
}

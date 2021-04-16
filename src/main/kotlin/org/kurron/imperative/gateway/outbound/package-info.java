/**
 * The "ports" portion of the Ports and Adapters architecture. Any interactions outside of the application process live here.  The core will reference gateway interfaces that hide many of the details of the interaction.  Keeping the separation between the core and outbound interactions aids in testing.
 */
package org.kurron.imperative.gateway.outbound;
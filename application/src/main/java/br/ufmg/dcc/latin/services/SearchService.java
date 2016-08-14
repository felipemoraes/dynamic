package br.ufmg.dcc.latin.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;

@Path("/search")
public class SearchService {
	
	  @GET
	  @Path("dynamic/{index}/{model}/")
	  public Response query(@PathParam("index") String index, @PathParam("model") String model, @Context UriInfo info) {
		  
		   return Response.status(200)
				.entity("query: dynamic search is called : " + index + " " + model +  "  :"
						+info.getQueryParameters().getFirst("query"))
				.build();

	  }

	  @POST
	  @Consumes("application/json")
	  @Path("feedback")
	  public Response feeback(String json) {
		  
		   return Response.status(200)
				.entity("feedback: search is called : " + json )
				.build();

	  }
	  
}

package org.lebastudios.theroundtable.server.requests;

import com.google.gson.Gson;
import org.lebastudios.theroundtable.communications.AppHttpClient;
import org.lebastudios.theroundtable.server.Server;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public class Licenses
{
    public static Boolean isLicenseValid(String license)
    {
        try (var client = AppHttpClient.getInstance().newClient())
        {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(Server.BASE_URL + "/licenses/validate?license_id=" + license))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            HashMap<String, Boolean> responseMap = new Gson().fromJson(response.body(), HashMap.class);

            return responseMap.get("message");
        }
        catch (Exception e)
        {
            System.err.println("An error ocurred while trying to validate the license: " + e.getMessage());
            return null;
        }
    }

    public static boolean has_license(String email, String password)
    {
        return false;
    }

    public static boolean login(String email, String password)
    {
        return false;
    }
}

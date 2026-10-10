export default async (req) => {
  const headers = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "Content-Type",
    "Access-Control-Allow-Methods": "POST, OPTIONS"
  };

  if (req.method === "OPTIONS") {
    return new Response(null, {
      status: 204,
      headers
    });
  }

  if (req.method !== "POST") {
    return Response.json(
      { error: "Method not allowed" },
      {
        status: 405,
        headers
      }
    );
  }

  try {
    const body = await req.json();

    const text = String(body.text || "").trim();
    const source = body.source || "auto";
    const target = body.target || "en";

    if (!text) {
      return Response.json(
        { error: "No text provided" },
        {
          status: 400,
          headers
        }
      );
    }

    // Temporary demo response.
    // Later this block will call Google Cloud Translation.
    let translatedText = `[TEST ${source} → ${target}] ${text}`;

    if (
      text.toLowerCase() === "привет" &&
      target === "en"
    ) {
      translatedText = "Hello";
    }

    return Response.json(
      {
        translatedText,
        source,
        target,
        engine: "netlify-demo"
      },
      {
        status: 200,
        headers
      }
    );

  } catch (error) {
    return Response.json(
      {
        error: "Invalid request",
        details: error.message
      },
      {
        status: 400,
        headers
      }
    );
  }
};

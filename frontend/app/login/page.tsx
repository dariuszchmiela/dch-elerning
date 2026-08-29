"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        setError("Invalid email or password");
        return;
      }

      const data = await response.json();
      localStorage.setItem("token", data.token);
      router.push("/me");
    } finally {
      setLoading(false);
    }
  }

  return (
      <form onSubmit={handleSubmit} className="flex flex-col gap-4 max-w-sm mx-auto mt-20 p-6">
        <h1 className="text-xl font-bold">Log in</h1>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" className="border border-gray-300 rounded px-3 py-2" />
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Password" className="border border-gray-300 rounded px-3 py-2" />
        {error && <p className="text-red-600 text-sm">{error}</p>}
        <button type="submit" disabled={loading} className="bg-blue-600 text-white rounded px-4 py-2 hover:bg-blue-700 disabled:bg-gray-400">
          {loading ? "Logging in..." : "Log in"}
        </button>
        <p className="text-sm text-center">
          No account? <Link href="/register" className="text-blue-600 hover:underline">Register</Link>
        </p>
      </form>
  );
}
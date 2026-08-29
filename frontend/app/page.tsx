import Link from "next/link";

export default function Home() {
  return (
      <div className="flex flex-col gap-6 max-w-sm mx-auto mt-32 p-6 text-center">
        <h1 className="text-3xl font-bold">DCH E-learning</h1>
        <p className="text-gray-600">Interactive learning platform</p>
        <div className="flex gap-4 justify-center">
          <Link href="/login" className="bg-blue-600 text-white rounded px-4 py-2 hover:bg-blue-700">
            Log in
          </Link>
          <Link href="/register" className="bg-gray-200 rounded px-4 py-2 hover:bg-gray-300">
            Register
          </Link>
        </div>
      </div>
  );
}